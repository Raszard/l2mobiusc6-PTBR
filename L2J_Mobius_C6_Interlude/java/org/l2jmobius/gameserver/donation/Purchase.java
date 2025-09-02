package org.l2jmobius.gameserver.donation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.sql.CharNameTable;

public class Purchase
{
	private final int _id;
	private final int _player;
	private final int _product;
	private final int _quantity;
	private final int _unitPrice;
	private long _date;
	private long _mpPaymentId;
	private String _mpPreferenceId;
	private String _email;
	private String _message;
	private String _qrCode;
	private String _ipAddress;
	private boolean _busy;
	private int _emailCount;
	private PurchaseStatus _status;
	private PaymentMethod _paymentMethod;
	
	public enum PurchaseStatus
	{
		CREATED("Criada", "Compra criada. Aguardando pagamento"),
		WAITING("Aguardando", "Aguardando pagamento"),
		CANCELED("Cancelada", "Cancelada por iniciativa do player"),
		EXPIRED("Expirada", "Prazo de pagamento expirou"),
		COMPLETED("Concluída", "Pagamento rebido e itens entregues"),
		FAILED("Falhou", "Houve um problema no pagamento"),
		// interno
		OFFLINE("Offline", "Player estava offline e não recebeu os itens."),
		HIDDEN("Oculta", "Compra não mais visível para o player"),
		FINISHING("Finalizando", "Realizando checagens finais antes do encerramento");
		
		private final String _status;
		private final String _desc;
		
		private PurchaseStatus(String status, String desc)
		{
			_status = status;
			_desc = desc;
		}
		
		public final String getName()
		{
			return _status;
		}
		
		public final String getDesc()
		{
			return _desc;
		}
	}
	
	public enum PaymentMethod
	{
		PIX("Pix"),
		LINK("Link");
		
		private final String _name;
		
		private PaymentMethod(String name)
		{
			_name = name;
		}
		
		public final String getName()
		{
			return _name;
		}
	}
	
	public Purchase(ResultSet rs) throws SQLException
	{
		final String paymentMethod = rs.getString("payment_method");
		if (paymentMethod != null)
		{
			_paymentMethod = PaymentMethod.valueOf(paymentMethod);
		}
		
		_id = rs.getInt("purchase_id");
		_player = rs.getInt("player_id");
		_product = rs.getInt("product_id");
		_quantity = rs.getInt("quantity");
		_unitPrice = rs.getInt("unit_price");
		_date = rs.getLong("date");
		_email = rs.getString("email");
		_mpPaymentId = rs.getLong("mp_payment_id");
		_mpPreferenceId = rs.getString("mp_preference_id");
		_status = PurchaseStatus.valueOf(rs.getString("status"));
	}
	
	public Purchase(int id, int player, int product, int quantity, String email, String ipAddress, PurchaseStatus status, PaymentMethod method)
	{
		_id = id;
		_player = player;
		_product = product;
		_quantity = quantity;
		_unitPrice = Config.DONATION_PURCHASABLE_ITEMS.get(_product);
		_email = email;
		_ipAddress = ipAddress;
		_status = status;
		_date = System.currentTimeMillis();
		_paymentMethod = method;
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public final int getPlayerId()
	{
		return _player;
	}
	
	public final int getProductId()
	{
		return _product;
	}
	
	public final int getQuantity()
	{
		return _quantity;
	}
	
	public final int getUnitPrice()
	{
		return _unitPrice;
	}
	
	public final int getTotalPrice()
	{
		return _unitPrice * _quantity;
	}
	
	public long getDate()
	{
		return _date;
	}
	
	public void updateDate(long value)
	{
		_date = value;
	}
	
	public String getPlayerEmail()
	{
		return _email;
	}
	
	public String getIpAddress()
	{
		return _ipAddress;
	}
	
	public void setPlayerEmail(String address)
	{
		_email = address;
		DonationManager.getInstance().update(this);
	}
	
	public PurchaseStatus getStatus()
	{
		return _status;
	}
	
	public void changeStatus(PurchaseStatus newStatus)
	{
		_status = newStatus;
		// Esses valores não são mais úteis
		if ((newStatus != PurchaseStatus.WAITING) && (newStatus != PurchaseStatus.CREATED) && (newStatus != PurchaseStatus.FINISHING))
		{
			_qrCode = null;
			_mpPreferenceId = null;
		}
		
		DonationManager.getInstance().update(this);
	}
	
	public String getPlayerName()
	{
		return CharNameTable.getInstance().getPlayerName(_player);
	}
	
	/**
	 * @return : ID do pagamento no Mercado Pago
	 */
	public long getMpPaymentId()
	{
		return _mpPaymentId;
	}
	
	public void setMpPaymentId(long id)
	{
		_mpPaymentId = id;
	}
	
	public String getMpPreferenceId()
	{
		return _mpPreferenceId;
	}
	
	public void setMpPreferenceId(String id)
	{
		_mpPreferenceId = id;
	}
	
	public String getMpPreferenceLink()
	{
		return _mpPreferenceId != null ? ("https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=" + _mpPreferenceId) : null;
	}
	
	public String getProductName()
	{
		return ItemTable.getInstance().getTemplate(_product).getName();
	}
	
	public void setQrCode(String value)
	{
		_qrCode = value;
	}
	
	public String getQrCode()
	{
		return getPaymentMethod() == PaymentMethod.PIX ? _qrCode : getMpPreferenceLink();
	}
	
	public long getExpiration()
	{
		// O prazo mínimo para o pix é de 30 minutos
		// https://www.mercadopago.com.br/developers/pt/docs/checkout-api/integration-configuration/integrate-with-pix
		if (_paymentMethod == PaymentMethod.PIX)
		{
			return getDate() + TimeUnit.MINUTES.toMillis(Math.max(Config.DONATION_MP_PIX_EXPIRATION_TIME, 30));
		}
		
		return getDate() + TimeUnit.MINUTES.toMillis(Math.max(Config.DONATION_MP_LINK_EXPIRATION_TIME, 1));
	}
	
	public boolean timeExpired()
	{
		return System.currentTimeMillis() > getExpiration();
	}
	
	public void setMessage(String value)
	{
		_message = value;
	}
	
	public String getMessage()
	{
		return _message;
	}
	
	public PaymentMethod getPaymentMethod()
	{
		return _paymentMethod;
	}
	
	public int getEmailCoint()
	{
		return _emailCount;
	}
	
	public void logEmailSending()
	{
		_emailCount++;
	}
	
	public void setBusy(Boolean status)
	{
		_busy = status;
	}
	
	public boolean isBusy()
	{
		return _busy;
	}
	
	public String resume()
	{
		return "" + getQuantity() + " - " + getProductName() + " - " + getTotalPrice() + ",00 BRL";
	}
}