package org.l2jmobius.gameserver.donation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentAdditionalInfoRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResultsResourcesPage;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.donation.Purchase.PaymentMethod;
import org.l2jmobius.gameserver.donation.Purchase.PurchaseStatus;

public class MercadoPagoAPI
{
	private static final Logger LOGGER = Logger.getLogger(MercadoPagoAPI.class.getName());
	
	public void reload()
	{
		MercadoPagoConfig.setAccessToken(Config.DONATION_MP_TOKEN);
	}
	
	public void checkPurchase(Purchase purchase)
	{
		if (purchase.getPaymentMethod() == PaymentMethod.PIX)
		{
			if (!Config.DONATION_MP_PIX)
			{
				return;
			}
			
			ThreadPool.execute(() -> getPayment(purchase));
		}
		else
		{
			if (!Config.DONATION_MP_LINK)
			{
				return;
			}
			
			ThreadPool.execute(() -> searchPayment(purchase));
		}
	}
	
	public void sendPurchase(Purchase purchase)
	{
		if (purchase.getPaymentMethod() == PaymentMethod.PIX)
		{
			if (!Config.DONATION_MP_PIX)
			{
				return;
			}
			
			ThreadPool.execute(() -> createPayment(purchase));
		}
		else
		{
			if (!Config.DONATION_MP_LINK)
			{
				return;
			}
			
			ThreadPool.execute(() -> createPreference(purchase));
		}
	}
	
	/*
	 * Cancela o Payment ou Preference para evitar pagamentos de compras canceladas, expiradas ou já pagas Fazemos uma tentativa. Não lidamos com erros.
	 */
	public void closePurchase(Purchase purchase)
	{
		if (purchase.getPaymentMethod() == PaymentMethod.PIX)
		{
			if (purchase.getMpPaymentId() == 0)
			{
				return;
			}
			
			ThreadPool.execute(() -> closePayment(purchase));
		}
		else
		{
			if (purchase.getMpPreferenceId() == null)
			{
				return;
			}
			
			ThreadPool.execute(() -> closePreference(purchase));
		}
	}
	
	/*
	 * Atualizar a data de vencimento da preference para evitar que ainda continue disponível para pagamento
	 */
	private static void closePreference(Purchase purchase)
	{
		if (purchase.timeExpired())
		{
			return;
		}
		
		try
		{
			final PreferenceClient client = new PreferenceClient();
			final PreferenceRequest updateRequest = PreferenceRequest.builder().dateOfExpiration(OffsetDateTime.now().plusSeconds(15)).build();
			client.update(purchase.getMpPreferenceId(), updateRequest);
		}
		catch (MPApiException e)
		{
		}
		catch (MPException e)
		{
		}
	}
	
	/*
	 * https://www.mercadopago.com.br/developers/pt/reference/payments/_payments_id/put
	 */
	private static void closePayment(Purchase purchase)
	{
		if (purchase.timeExpired())
		{
			return;
		}
		
		try
		{
			final PaymentClient client = new PaymentClient();
			client.cancel(purchase.getMpPaymentId());
		}
		catch (MPApiException e)
		{
		}
		catch (MPException e)
		{
		}
	}
	
	/*
	 * https://www.mercadopago.com.ar/developers/pt/docs/checkout-pro/integrate-preferences https://www.mercadopago.com.br/developers/pt/reference/preferences/_checkout_preferences/post
	 */
	private static void createPreference(Purchase purchase)
	{
		if (!isThreadSafe(purchase))
		{
			return;
		}
		
		try
		{
			purchase.setBusy(true);
			
			final PreferenceItemRequest itemRequest = PreferenceItemRequest.builder().id(String.valueOf(purchase.getId())).title(purchase.getProductName()).quantity(purchase.getQuantity()).currencyId(Config.DONATION_MP_CURRENCY).unitPrice(new BigDecimal(purchase.getUnitPrice())).build();
			
			final PreferencePayerRequest payerRequest = PreferencePayerRequest.builder().name(purchase.getPlayerName()).email(purchase.getPlayerEmail()).build();
			
			final OffsetDateTime eol = OffsetDateTime.ofInstant(Instant.ofEpochMilli(purchase.getExpiration()), ZoneId.systemDefault());
			final PreferenceRequest request = PreferenceRequest.builder().items(List.of(itemRequest)).payer(payerRequest).dateOfExpiration(eol).statementDescriptor(Config.DONATION_SERVER_NAME).expires(true).externalReference(String.valueOf(purchase.getId())) // Preference não é associada ao Payment,
																																																																	// temos que fazer isso
				.build();
			
			final PreferenceClient client = new PreferenceClient();
			final Preference preference = client.create(request);
			DonationManager.getInstance().handleMPPreferenceResponse(purchase, preference);
		}
		catch (MPApiException e)
		{
			logException(e, purchase);
		}
		catch (MPException e)
		{
			logException(e, purchase);
		}
		finally
		{
			purchase.setBusy(false);
		}
	}
	
	/*
	 * https://www.mercadopago.com.br/developers/pt/docs/checkout-api/integration-configuration/integrate-with-pix MercadoPagoConfig.setSocketTimeout(20000); // default
	 */
	private static void createPayment(Purchase purchase)
	{
		if (!isThreadSafe(purchase))
		{
			return;
		}
		
		try
		{
			purchase.setBusy(true);
			
			final MPRequestOptions requestOptions = MPRequestOptions.builder().customHeaders(Map.of("x-idempotency-key", String.valueOf(purchase.getId()))).build();
			
			final PaymentPayerRequest payerRequest = PaymentPayerRequest.builder().firstName(purchase.getPlayerName()).email(purchase.getPlayerEmail())
				// .identification(IdentificationRequest.builder().type("CPF").number("19119119100").build())
				.build();
			
			final OffsetDateTime eol = OffsetDateTime.ofInstant(Instant.ofEpochMilli(purchase.getExpiration()), ZoneId.systemDefault());
			final PaymentCreateRequest paymentRequest = PaymentCreateRequest.builder().transactionAmount(new BigDecimal(purchase.getTotalPrice())).description(purchase.getProductName()).paymentMethodId("pix").dateOfExpiration(eol).payer(payerRequest).additionalInfo(PaymentAdditionalInfoRequest.builder().ipAddress(purchase.getIpAddress()).build()).build();
			
			final PaymentClient client = new PaymentClient();
			final Payment payment = client.create(paymentRequest, requestOptions);
			DonationManager.getInstance().handleMPPaymentResponse(purchase, payment);
		}
		catch (MPApiException e)
		{
			logException(e, purchase);
		}
		catch (MPException e)
		{
			logException(e, purchase);
		}
		finally
		{
			purchase.setBusy(false);
		}
	}
	
	/*
	 * https://www.mercadopago.com.br/developers/pt/reference/payments/_payments_search/get
	 */
	private static void getPayment(Purchase purchase)
	{
		if (purchase.getStatus() == PurchaseStatus.EXPIRED)
		{
			DonationManager.getInstance().handleMPCheck(purchase);
			return;
		}
		
		if (!isThreadSafe(purchase))
		{
			return;
		}
		
		try
		{
			purchase.setBusy(true);
			
			final PaymentClient paymentClient = new PaymentClient();
			final Payment payment = paymentClient.get(purchase.getMpPaymentId());
			DonationManager.getInstance().handleMPCheck(purchase, payment);
		}
		catch (MPApiException e)
		{
			logException(e, purchase);
		}
		catch (MPException e)
		{
			logException(e, purchase);
		}
		finally
		{
			purchase.setBusy(false);
		}
	}
	
	/*
	 * https://www.mercadopago.com.br/developers/pt/reference/payments/_payments_search/get
	 */
	private static void searchPayment(Purchase purchase)
	{
		if (purchase.getStatus() == PurchaseStatus.EXPIRED)
		{
			DonationManager.getInstance().handleMPCheck(purchase);
			return;
		}
		
		if (!isThreadSafe(purchase))
		{
			return;
		}
		
		try
		{
			purchase.setBusy(true);
			
			final PaymentClient paymentClient = new PaymentClient();
			final Map<String, Object> filters = new HashMap<>();
			filters.put("sort", "date_created");
			filters.put("criteria", "desc");
			filters.put("external_reference", purchase.getId());
			filters.put("range", "date_created");
			filters.put("begin_date", Instant.ofEpochMilli(purchase.getDate()).toString());
			filters.put("end_date", Instant.ofEpochMilli(purchase.getExpiration()).toString());
			
			final MPSearchRequest searchRequest = MPSearchRequest.builder().offset(0).limit(1).filters(filters).build(); // limit 0?
			final MPResultsResourcesPage<Payment> search = paymentClient.search(searchRequest);
			
			if (search.getResults().isEmpty())
			{
				DonationManager.getInstance().handleMPCheck(purchase);
			}
			else
			{
				DonationManager.getInstance().handleMPCheck(purchase, search.getResults().get(0));
			}
		}
		catch (MPApiException e)
		{
			logException(e, purchase);
		}
		catch (MPException e)
		{
			logException(e, purchase);
		}
		finally
		{
			purchase.setBusy(false);
		}
	}
	
	/*
	 * Para evitar problemas de concorrência Seria melhor verificar apenas pela purchase atual ao invés de todas?
	 */
	private static boolean isThreadSafe(Purchase p)
	{
		if (DonationManager.getInstance().getPurchases(p.getPlayerId()).stream().anyMatch(Purchase::isBusy))
		{
			DonationManager.getInstance().handleMPException(p, false);
			return false;
		}
		
		return true;
	}
	
	private static void logException(MPApiException e, Purchase p)
	{
		switch (e.getApiResponse().getStatusCode())
		{
			case 404:
				DonationManager.getInstance().handleMPCheck(p);
				break;
			
			case 500:
				DonationManager.getInstance().handleMPException(p, false);
				LOGGER.warning("A API do Mercado Pago não está funcionando agora. É necessário aguardar.");
				break;
			
			default:
				DonationManager.getInstance().handleMPException(p, true);
				LOGGER.warning("Falha ao enviar requisição da purchase #{" + p.getId() + "} para à API do Mercado Pago.");
				LOGGER.warning("Status: {" + e.getApiResponse().getStatusCode() + "}, Content: {" + e.getApiResponse().getContent() + "}");
		}
	}
	
	private static void logException(MPException e, Purchase p)
	{
		LOGGER.warning("Falha ao enviar requisição da purchase #{" + p.getId() + "} para à API do Mercado Pago.");
		DonationManager.getInstance().handleMPException(p, true);
	}
	
	/*
	 * Sem utilidade no momento https://www.mercadopago.com.br/developers/pt/reference/preferences/_checkout_preferences_id/get
	 */
	// private static void getPreference(Purchase purchase)
	// {
	// if (purchase.getStatus() == PurchaseStatus.EXPIRED)
	// {
	// DonationManager.getInstance().handleMPCheck(purchase);
	// return;
	// }
	//
	// try
	// {
	// final PreferenceClient client = new PreferenceClient();
	// final Preference preference = client.get(purchase.getMpPreferenceId());
	// }
	// catch (MPApiException e)
	// {
	// logException(e, purchase);
	// }
	// catch (MPException e)
	// {
	// logException(e, purchase);
	// }
	// }
	
	public static MercadoPagoAPI getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MercadoPagoAPI INSTANCE = new MercadoPagoAPI();
	}
}