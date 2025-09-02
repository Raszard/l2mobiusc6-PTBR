package org.l2jmobius.gameserver.donation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.zxing.WriterException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.io.DDSConverter;
import org.l2jmobius.commons.util.ArraysUtil;
import org.l2jmobius.commons.util.Pagination;
import org.l2jmobius.commons.util.StringUtil;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.donation.Purchase.PaymentMethod;
import org.l2jmobius.gameserver.donation.Purchase.PurchaseStatus;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.PledgeCrest;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;

public class DonationManager
{
	private static final Logger LOGGER = Logger.getLogger(DonationManager.class.getName());
	
	private static final String LOAD_PURCHASES = "SELECT * FROM donations";
	private static final String NEW_PURCHASE = "INSERT INTO donations (`purchase_id`, `player_id`,`email`,`product_id`,`quantity`,`unit_price`,`date`,`status`) VALUES (?,?,?,?,?,?,?,?)";
	private static final String DELETE_PURCHASE = "DELETE FROM donations WHERE purchase_id=?";
	private static final String UPDATE_PURCHASE = "UPDATE donations SET mp_payment_id=?, mp_preference_id=?, payment_method=?, email=?, status=? WHERE purchase_id=?";
	
	// private static final int HTML_ID = 9999;
	
	private static final String HTML_PATH = "data/html/ayengine/donation/";
	private static final String HTML_EMPTY_TABLE = "<table><tr><td></td></tr><tr><td width=70>---</td><td width=115>---</td><td width=50>---</td><td width=75>---</td></tr><tr><td></td></tr></table>";
	private static final String HTML_LINE = "<img src=L2UI.SquareGray width=280 height=1>";
	private static final String HTML_MESSAGE_1 = "<br><table bgcolor=000000 cellpadding=4><tr><td align=center width=280><font color=deba73>%s</font></td></tr><tr><td>%s</td></tr></table>";
	private static final String HTML_MESSAGE_2 = "<br><table bgcolor=000000 cellpadding=4><tr><td><font color=deba73>%s</font<</td></tr></table>";
	
	private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
	private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);
	
	private final static Map<Integer, List<Purchase>> _playersPurchases = new ConcurrentHashMap<>();
	private final static Map<Integer, Purchase> _waitingPurchases = new ConcurrentHashMap<>();
	
	public DonationManager()
	{
		reload();
		restore();
	}
	
	public void reload()
	{
		MercadoPagoAPI.getInstance().reload();
	}
	
	public boolean isEnabled()
	{
		return (Config.DONATION_PURCHASABLE_ITEMS != null) && !Config.DONATION_PURCHASABLE_ITEMS.isEmpty() && !Config.DONATION_MP_TOKEN.isEmpty() && (Config.DONATION_MP_PIX || Config.DONATION_MP_LINK);
	}
	
	public void handleBypass(Player player, String command)
	{
		try
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			final String action = st.nextToken();
			
			if (action.equals("htm"))
			{
				showCustomWindow(player, st.nextToken());
			}
			else if (action.equals("email"))
			{
				if (st.nextToken().equals("view"))
				{
					// Remover compras pendentes
					if (_waitingPurchases.containsKey(player.getObjectId()))
					{
						_waitingPurchases.remove(player.getObjectId());
					}
					
					showEmailWindow(player, getEmail(player.getAccountName()));
				}
				else
				{
					setPlayerEmail(player, st.nextToken());
				}
			}
			else if (action.equals("index"))
			{
				showIndexWindow(player);
			}
			else
			{
				final int id = Integer.valueOf(st.nextToken());
				if (action.equals("buy"))
				{
					
					for (Purchase purchase : getPurchases(player.getObjectId()))
					{
						if (purchase.getStatus() == Purchase.PurchaseStatus.WAITING)
						{
							showFloodWindow(player, 0);
							return;
						}
					}
					newPurchase(player, id, st.nextToken(), st.hasMoreTokens() ? Integer.valueOf(st.nextToken()) : 0);
				}
				else if (action.equals("check"))
				{
					checkPaymentStatus(player, id);
				}
				else if (action.equals("status"))
				{
					showPurchaseStatusWindow(player, id);
				}
				else if (action.equals("hide"))
				{
					hidePurchase(player, id);
				}
				else if (action.equals("history"))
				{
					showPurchaseHistoryWindow(player, id);
				}
				else if (action.equals("cancel"))
				{
					cancelPurchase(player, id);
				}
				else if (action.equals("qrcode"))
				{
					showQrCodeWindow(player, id);
				}
				else if (action.equals("link"))
				{
					showLinkWindow(player, id, st.nextToken().equals("true"));
				}
				else if (action.equals("confirm"))
				{
					showConfirmActionWindow(player, id, st.nextToken());
				}
				else if (action.equals("closeqr"))
				{
					showPurchaseStatusWindow(player, id);
					player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Entrega a compra feita por um player que estava offline no momento em que os itens seriam entregues
	 * @param player
	 */
	public void offlinePlayer(Player player)
	{
		for (Purchase p : getPurchases(player.getObjectId()))
		{
			if (p.getStatus() != PurchaseStatus.OFFLINE)
			{
				continue;
			}
			
			player.addItem("DonationManager", p.getProductId(), p.getQuantity(), player, true);
			p.changeStatus(PurchaseStatus.COMPLETED);
		}
	}
	
	public List<Purchase> getPurchases(int playerId)
	{
		return _playersPurchases.computeIfAbsent(playerId, k -> new ArrayList<>());
	}
	
	public void showIndexWindow(Player player)
	{
		if (!isEnabled())
		{
			showCustomWindow(player, "index_disabled.htm");
			return;
		}
		
		if (Config.DONATION_PURCHASABLE_ITEMS.size() != 1)
		{
			showCustomWindow(player, "index_several.htm");
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		// html.setItemId(HTML_ID);
		
		final NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		nf.setCurrency(Currency.getInstance(Config.DONATION_MP_CURRENCY));
		
		final Entry<Integer, Integer> entry = Config.DONATION_PURCHASABLE_ITEMS.entrySet().iterator().next();
		html.setFile(HTML_PATH + "index_single.htm");
		html.replace("%icon%", ItemTable.getInstance().getTemplate(entry.getKey()).getIcon());
		html.replace("%item_id%", entry.getKey());
		html.replace("%price%", 1);
		
		if (Config.DONATION_MP_PIX && Config.DONATION_MP_LINK)
		{
			html.replace("%payment%", "PIX;Link");
		}
		else if (Config.DONATION_MP_PIX)
		{
			html.replace("%payment%", "PIX");
		}
		else if (Config.DONATION_MP_LINK)
		{
			html.replace("%payment%", "Link");
		}
		
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/*
	 * Não é necessário cancelar no mercado pago porque aqui já estamos no prazo final de vencimento do pagamento
	 */
	public void deleteOrExpire(Purchase purchase)
	{
		if (Config.DONATION_DELETE_EXPIRED)
		{
			delete(purchase);
		}
		else
		{
			purchase.changeStatus(PurchaseStatus.EXPIRED);
		}
	}
	
	public void delete(Purchase p)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_PURCHASE))
		{
			ps.setInt(1, p.getId());
			ps.execute();
			
			getPurchases(p.getPlayerId()).remove(p);
		}
		catch (Exception e)
		{
			LOGGER.warning("Não foi possível deletar a Purchase id #{" + p.getId() + "}.");
		}
	}
	
	public void update(Purchase p)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_PURCHASE))
		{
			ps.setLong(1, p.getMpPaymentId());
			ps.setString(2, p.getMpPreferenceId());
			ps.setString(3, p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null);
			ps.setString(4, p.getPlayerEmail());
			ps.setString(5, p.getStatus().name());
			ps.setInt(6, p.getId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.warning("Falhar ao atualizar a Purchase id #{" + p.getId() + "} do player {" + p.getPlayerName() + "}.");
		}
	}
	
	private void restore()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_PURCHASES);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final Purchase p = new Purchase(rs);
				if ((p.getStatus() == PurchaseStatus.WAITING) || (p.getStatus() == PurchaseStatus.CREATED))
				{
					DonationTaskManager.getInstance().add(p);
				}
				
				getPurchases(rs.getInt("player_id")).add(p);
			}
			
			LOGGER.info("Loaded {" + _playersPurchases.values().stream().mapToInt(List::size).sum() + "} donations.");
		}
		catch (Exception e)
		{
			LOGGER.warning("Não foi possível restaurar as doações.");
		}
	}
	
	private void store(Purchase p)
	{
		if (_waitingPurchases.containsKey(p.getPlayerId()))
		{
			p.updateDate(System.currentTimeMillis());
			_waitingPurchases.remove(p.getPlayerId());
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(NEW_PURCHASE))
		{
			ps.setInt(1, p.getId());
			ps.setInt(2, p.getPlayerId());
			ps.setString(3, p.getPlayerEmail());
			ps.setInt(4, p.getProductId());
			ps.setInt(5, p.getQuantity());
			ps.setInt(6, p.getUnitPrice());
			ps.setLong(7, p.getDate());
			ps.setString(8, p.getStatus().name());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.warning("Não foi possível criar a Purchase id #{" + p.getId() + "} para o player {" + p.getPlayerName() + "}.");
		}
		finally
		{
			DonationTaskManager.getInstance().add(p);
			getPurchases(p.getPlayerId()).add(p);
		}
	}
	
	private Purchase getPurchase(int playerId, int purchaseId)
	{
		return getPurchases(playerId).stream().filter(p -> p.getId() == purchaseId).findFirst().orElse(null);
	}
	
	private static void showCustomWindow(Player player, String file)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(HTML_PATH + file);
		// html.setItemId(HTML_ID);
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static void showCustomWindow(int playerId, String file)
	{
		showCustomWindow(World.getInstance().getPlayer(playerId), file);
	}
	
	private static void showConfirmActionWindow(Player player, int purchaseId, String action)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(HTML_PATH + "action.htm");
		// html.setItemId(HTML_ID);
		html.replace("%action%", action);
		html.replace("%purchase%", purchaseId);
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static void showEmailWindow(Player player, String address)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(HTML_PATH + "email.htm");
		// html.setItemId(HTML_ID);
		
		if (_waitingPurchases.containsKey(player.getObjectId()))
		{
			html.replace("%address%", "<td>Você será <font color=LEVEL>redirecionado ao pagamento</font> logo após concluir essa etapa.</td>");
		}
		else if (address == null)
		{
			html.replace("%address%", "<td align=center>Por favor, insira um endereço válido.</td>");
		}
		else
		{
			html.replace("%address%", "<td align=center>Endereço atual: <font color=LEVEL>" + address + "</font></td>");
		}
		
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showPurchaseStatusWindow(Player player, int id)
	{
		final Purchase p = getPurchase(player.getObjectId(), id);
		if (p == null)
		{
			return;
		}
		showPurchaseStatusWindow(player, p);
	}
	
	private static void showFloodWindow(Player player, int purchaseId)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(HTML_PATH + "flood.htm");
		html.replace("%id%", purchaseId);
		html.replace("%bypass%", "bypass donation " + (purchaseId != 0 ? "status " + purchaseId : "index"));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static void showPurchaseStatusWindow(Player player, Purchase p)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		if ((p.getStatus() == PurchaseStatus.WAITING) || (p.getStatus() == PurchaseStatus.CREATED))
		{
			html.setFile(HTML_PATH + "status_waiting.htm");
			if (p.getMessage() != null)
			{
				html.replace("%msg%", p.getMessage());
				p.setMessage(null);
			}
			else
			{
				html.replace("%msg%", "");
			}
			
			if (p.getPaymentMethod() == PaymentMethod.LINK)
			{
				html.replace("%link%", Config.DONATION_MP_LINK ? "<td><button value=\"Link\" action=\"bypass donation link %id% false\" width=75 height=21 back=L2UI_ch3.Btn1_normalOn fore=L2UI_ch3.Btn1_normal></td>" : "");
			}
			else
			{
				html.replace("%link%", "");
			}
			
			if ((p.getPaymentMethod() == PaymentMethod.PIX) || (p.getQrCode() != null))
			{
				html.replace("%qrcode%", Config.DONATION_MP_PIX ? "<td><button value=\"QR Code\" action=\"bypass donation qrcode %id%\" width=75 height=21 back=L2UI_ch3.Btn1_normalOn fore=L2UI_ch3.Btn1_normal></td>" : "");
			}
			else
			{
				html.replace("%qrcode%", "");
			}
			
			if (p.getStatus() == PurchaseStatus.WAITING)
			{
				html.replace("%check%", "<td><button value=\"Paguei\" action=\"bypass -h donation check %id%\" width=75 height=21 back=L2UI_ch3.Btn1_normalOn fore=L2UI_ch3.Btn1_normal></td>");
			}
			else
			{
				html.replace("%check%", "");
			}
			
			final long expiration = TimeUnit.MILLISECONDS.toMinutes(p.getExpiration() - System.currentTimeMillis());
			html.replace("%expiration%", +expiration > 1 ? expiration + " minutos" : " menos de 1 minuto");
		}
		else
		{
			html.setFile(HTML_PATH + "status_others.htm");
			html.replace("%hide%", Config.DONATION_HIDE_COMPLETED ? "<td width=52><a action=\"bypass donation confirm %id% hide\">Excluir</a></td>" : "");
		}
		
		// html.setItemId(HTML_ID);
		html.replace("%id%", p.getId());
		html.replace("%id_mp%", p.getMpPaymentId());
		html.replace("%method%", p.getPaymentMethod().getName());
		html.replace("%email%", p.getPlayerEmail());
		html.replace("%status%", p.getStatus().getDesc());
		html.replace("%date%", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(p.getDate()));
		html.replace("%player%", player.getName());
		html.replace("%resume%", p.resume());
		player.sendPacket(html);
	}
	
	private void showPurchaseHistoryWindow(Player player, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(HTML_PATH + "history.htm");
		// html.setItemId(HTML_ID);
		
		final Pagination<Purchase> pagination = new Pagination<>(getPurchases(player.getObjectId()).stream(), page, 9, p -> p.getStatus() != PurchaseStatus.HIDDEN, Comparator.comparing(Purchase::getDate).reversed());
		for (Purchase p : pagination)
		{
			pagination.append("<table><tr><td></td></tr><tr><td width=70>");
			pagination.append(new SimpleDateFormat("dd-MM-yyyy").format(p.getDate()));
			pagination.append("</td><td width=115>");
			pagination.append(StringUtil.trimAndDress(p.getProductName(), 18));
			pagination.append("</td><td width=50>");
			pagination.append(p.getTotalPrice(), " BRL");
			pagination.append("</td><td width=75>");
			pagination.append(String.format("<a action=\"bypass donation status %d %d\">", p.getId(), page), p.getStatus().getName(), "<a>");
			pagination.append("</td></tr><tr><td></td></tr></table>");
			pagination.append(HTML_LINE);
		}
		
		if (pagination.isEmpty())
		{
			pagination.append(HTML_EMPTY_TABLE);
		}
		else if (pagination.getTotalEntries() > 9)
		{
			pagination.generatePages("bypass donation history %page%");
		}
		
		html.replace("%table%", pagination.getContent());
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static void showCheckoutWindow(Player player, Purchase purchase)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(HTML_PATH + (purchase.getPaymentMethod() == PaymentMethod.PIX ? "checkout_pix.htm" : "checkout_link.htm"));
		// html.setItemId(HTML_ID);
		html.replace("%id%", purchase.getId());
		html.replace("%resume%", purchase.resume());
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showQrCodeWindow(Player player, int purchaseId)
	{
		final Purchase p = getPurchase(player.getObjectId(), purchaseId);
		if (p == null)
		{
			return;
		}
		
		showQrCodeWindow(player, p);
	}
	
	private void showQrCodeWindow(Player player, Purchase p)
	{
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		if (!isEnabled())
		{
			showIndexWindow(player);
			return;
		}
		
		if (p.getStatus() == PurchaseStatus.EXPIRED)
		{
			showPurchaseStatusWindow(player, p);
			return;
		}
		
		if ((p.getStatus() == PurchaseStatus.WAITING) && (p.getPaymentMethod() == PaymentMethod.PIX) && (p.getQrCode() == null))
		{
			MercadoPagoAPI.getInstance().checkPurchase(p);
			showCustomWindow(player, "requesting.htm");
			return;
		}
		
		if (p.getStatus() == PurchaseStatus.CREATED)
		{
			MercadoPagoAPI.getInstance().sendPurchase(p);
			showCustomWindow(player, "requesting.htm");
			return;
		}
		
		try
		{
			final byte[] qrCode = DDSConverter.createQRCode(p.getQrCode());
			final NpcHtmlMessage html = new NpcHtmlMessage(5);
			final StringBuilder sb = new StringBuilder();
			
			if (!Config.DONATION_MP_PIX_ACCOUNT_OWNER.isEmpty())
			{
				sb.append("<tr><td align=center>TITULAR: " + Config.DONATION_MP_PIX_ACCOUNT_OWNER.toUpperCase() + "</td></tr>");
			}
			
			if (!Config.DONATION_MP_PIX_ACCOUNT_CPF.isEmpty())
			{
				sb.append("<tr><td align=center>CPF: " + Config.DONATION_MP_PIX_ACCOUNT_CPF + "</td></tr>");
			}
			
			if (!Config.DONATION_MP_PIX_ACCOUNT_BANK.isEmpty())
			{
				sb.append("<tr><td align=center>BANCO: " + Config.DONATION_MP_PIX_ACCOUNT_BANK.toUpperCase() + "</td></tr>");
			}
			
			html.setFile(HTML_PATH + "qrcode.htm");
			html.replace("%info%", sb.isEmpty() ? "" : "<br><br><table width=280 cellpadding=4>" + sb.toString() + "</table>");
			html.replace("%purchase%", p.getId());
			html.replace("%serverId%", Config.SERVER_ID);
			
			player.sendPacket(new PledgeCrest(p.getId(), qrCode));
			player.sendPacket(new TutorialShowHtml(html.getContent()));
		}
		catch (WriterException e)
		{
		}
	}
	
	private void showLinkWindow(Player player, int purchaseId, boolean sendMail)
	{
		final Purchase p = getPurchase(player.getObjectId(), purchaseId);
		if (p == null)
		{
			return;
		}
		
		if (!isEnabled())
		{
			showIndexWindow(player);
			return;
		}
		
		if (p.getStatus() == PurchaseStatus.EXPIRED)
		{
			showPurchaseStatusWindow(player, p);
			return;
		}
		
		if ((p.getStatus() == PurchaseStatus.WAITING) && (p.getMpPreferenceId() == null))
		{
			showCustomWindow(player, "requesting.htm");
			MercadoPagoAPI.getInstance().checkPurchase(p);
			return;
		}
		
		if (p.getStatus() == PurchaseStatus.CREATED)
		{
			showCustomWindow(player, "requesting.htm");
			MercadoPagoAPI.getInstance().sendPurchase(p);
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile(HTML_PATH + "link.htm");
		// html.setItemId(HTML_ID);
		html.replace("%id%", p.getId());
		html.replace("%resend%", Config.DONATION_MAXIMUM_NUMBER_EMAILS > 0 ? ("<td width=64><a action=\"bypass -h donation link " + p.getId() + " true\">Reenviar</a></td>") : "");
		
		if (sendMail)
		{
			if ((p.getEmailCoint() >= Config.DONATION_MAXIMUM_NUMBER_EMAILS) || (Config.DONATION_MAXIMUM_NUMBER_EMAILS == 0))
			{
				html.replace("%msg%", "Vários e-mails já foram enviados para você.");
			}
			else
			{
				if ((p.getDate() + TimeUnit.MINUTES.toMillis(5)) < System.currentTimeMillis())
				{
					p.logEmailSending();
					html.replace("%msg%", "E-mail reenviado!");
					// MailersendAPI.sendPurchaseMail(p);
				}
				else if ((p.getExpiration() - System.currentTimeMillis()) < TimeUnit.MINUTES.toMillis(1))
				{
					html.replace("%msg%", "Não foi possível enviar um novo e-mail.");
				}
				else
				{
					html.replace("%msg%", "Um e-mail foi enviado recentemente.");
				}
			}
		}
		else
		{
			html.replace("%msg%", "Já enviamos seu e-mail.");
		}
		
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void setPlayerEmail(Player player, String email)
	{
		if (email.equals("0") || (email.length() > 44) || (email.length() < 6))
		{
			showEmailWindow(player, email);
			return;
		}
		
		final Matcher matcher = PATTERN.matcher(email);
		if (!matcher.matches())
		{
			player.sendMessage("Por favor, insira um e-mail válido.");
			showEmailWindow(player, email);
			return;
		}
		
		final String domain = email.substring(email.indexOf('@') + 1);
		if (!ArraysUtil.contains(Config.DONATION_ALLOWED_EMAILS, domain))
		{
			player.sendMessage("Por favor, insira um e-mail com domínio reconhecido.");
			showEmailWindow(player, email);
			return;
		}
		
		final Purchase p = _waitingPurchases.get(player.getObjectId());
		if (p != null)
		{
			p.setPlayerEmail(email);
			store(p);
			showPurchaseStatusWindow(player, p);
		}
		else
		{
			showEmailWindow(player, email);
		}
		setEmail(player.getAccountName(), email);
		LOGGER.info(player.getAccountName());
	}
	
	private void hidePurchase(Player player, int purchaseId)
	{
		if (!Config.DONATION_HIDE_COMPLETED)
		{
			return;
		}
		
		final Purchase p = getPurchase(player.getObjectId(), purchaseId);
		if (p == null)
		{
			return;
		}
		
		if (p.getStatus() != PurchaseStatus.HIDDEN)
		{
			p.changeStatus(PurchaseStatus.HIDDEN);
		}
		
		showPurchaseHistoryWindow(player, 1);
	}
	
	private void cancelPurchase(Player player, int id)
	{
		final Purchase p = getPurchase(player.getObjectId(), id);
		if ((p == null) || (p.getStatus() == PurchaseStatus.EXPIRED))
		{
			showPurchaseHistoryWindow(player, 1);
			return;
		}
		
		if (p.isBusy() || (p.getStatus() == PurchaseStatus.FINISHING))
		{
			showPurchaseStatusWindow(player, p);
			return;
		}
		
		if ((p.getStatus() == PurchaseStatus.WAITING) || (p.getStatus() == PurchaseStatus.CREATED))
		{
			MercadoPagoAPI.getInstance().closePurchase(p);
			p.changeStatus(PurchaseStatus.CANCELED);
		}
		
		showPurchaseStatusWindow(player, p);
		player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
	}
	
	private void newPurchase(Player player, int itemId, String paymentMethod, int quantity)
	{
		if (!isEnabled())
		{
			showIndexWindow(player);
			return;
		}
		
		if (!Config.DONATION_PURCHASABLE_ITEMS.containsKey(itemId) || (ItemTable.getInstance().getTemplate(itemId) == null))
		{
			return;
		}
		
		if (quantity == 0)
		{
			showIndexWindow(player);
			return;
		}
		
		final String email = getEmail(player.getAccountName());
		
		// Se ainda estamos definindo o e-mail, não consideramos flood
		// if ((email != null) && !player.getClient().performAction(FloodProtectors.DONATION_PAY))
		// {
		// showFloodWindow(player, 0);
		// return;
		// }
		
		// A compra sempre é salva. Vamos utilizá-la para o bypass de continuar a compra caso o email não exista
		final int purchaseId = IdManager.getInstance().getNextId();
		final PaymentMethod method = PaymentMethod.valueOf(paymentMethod.toUpperCase());
		final Purchase purchase = new Purchase(purchaseId, player.getObjectId(), itemId, quantity, email, player.getClient().getIp(), PurchaseStatus.CREATED, method);
		if (email == null)
		{
			_waitingPurchases.put(player.getObjectId(), purchase);
			showEmailWindow(player, null);
		}
		else
		{
			store(purchase);
			showPurchaseStatusWindow(player, purchase);
		}
		
		player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
	}
	
	private static void onCompletedPayment(Player player, Purchase p)
	{
		if (((p.getPaymentMethod() == PaymentMethod.LINK) && (p.getMpPreferenceId() == null)) || ((p.getPaymentMethod() == PaymentMethod.PIX) && (p.getMpPaymentId() == 0)))
		{
			return;
		}
		
		if ((player != null) && player.isOnline())
		{
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			p.changeStatus(PurchaseStatus.COMPLETED);
			
			final NpcHtmlMessage html = new NpcHtmlMessage(5);
			html.setFile(HTML_PATH + "thankyou.htm");
			// html.setItemId(HTML_ID);
			html.replace("%icon%", ItemTable.getInstance().getTemplate(p.getProductId()).getIcon());
			html.replace("%resume%", p.resume());
			player.addItem("DonationManager", p.getProductId(), p.getQuantity(), player, true);
			player.sendPacket(html);
		}
		else
		{
			// Vai receber no próximo login
			p.changeStatus(PurchaseStatus.OFFLINE);
		}
	}
	
	private void checkPaymentStatus(Player player, int purchaseId)
	{
		final Purchase purchase = getPurchase(player.getObjectId(), purchaseId);
		if (purchase == null)
		{
			return;
		}
		
		if (purchase.getStatus() == PurchaseStatus.CREATED)
		{
			purchase.setMessage(String.format(HTML_MESSAGE_2, (purchase.getPaymentMethod() == PaymentMethod.PIX ? "O QR Code" : "O link") + " para o pagamento ainda não foi gerado"));
			showPurchaseStatusWindow(player, purchase);
			return;
		}
		
		if (purchase.getStatus() == PurchaseStatus.EXPIRED)
		{
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			showPurchaseStatusWindow(player, purchase);
			return;
		}
		
		// if (!player.getClient().performAction(FloodProtectors.DONATION_CHECK))
		// {
		// showFloodWindow(player, purchaseId);
		// return;
		// }
		
		MercadoPagoAPI.getInstance().checkPurchase(purchase);
		showCustomWindow(player, "requesting.htm");
	}
	
	/*
	 * A Preference (link) foi criada no mercado pago, salvamos junto com a Purchase
	 */
	public void handleMPPreferenceResponse(Purchase p, Preference preference)
	{
		// link: preference.getInitPoint()
		p.logEmailSending();
		p.setMpPreferenceId(preference.getId());
		p.changeStatus(PurchaseStatus.WAITING);
		// MailersendAPI.sendPurchaseMail(p);
		showCheckoutWindow(World.getInstance().getPlayer(p.getPlayerId()), p);
	}
	
	/*
	 * O Payment foi criado no mercado pago, salvamos junto com a Purchase
	 */
	public void handleMPPaymentResponse(Purchase p, Payment payment)
	{
		final Player player = World.getInstance().getPlayer(p.getPlayerId());
		p.setMpPaymentId(payment.getId());
		p.changeStatus(PurchaseStatus.WAITING);
		p.setQrCode(payment.getPointOfInteraction().getTransactionData().getQrCode());
		showCheckoutWindow(player, p);
		showQrCodeWindow(player, p);
	}
	
	public void handleMPException(Purchase p, boolean clear)
	{
		// Caso não seja possível criar a compra no mercado pago, não salvamos ela
		if (clear)
		{
			delete(p);
		}
		
		showCustomWindow(p.getPlayerId(), "exception.htm");
	}
	
	/*
	 * Lida com pagamentos não encontrados de uma Preference
	 */
	public void handleMPCheck(Purchase p)
	{
		// Podemos estar aqui por causa da verificação final da task, então nesse caso não exibimos a janela porque o player pode não estar interagindo
		if (p.getStatus() == PurchaseStatus.FINISHING)
		{
			DonationTaskManager.getInstance().remove(p);
			deleteOrExpire(p);
			return;
		}
		
		final Player player = World.getInstance().getPlayer(p.getPlayerId());
		if ((player == null) || !player.isOnline())
		{
			return;
		}
		
		if (p.getStatus() != PurchaseStatus.EXPIRED)
		{
			p.setMessage(String.format(HTML_MESSAGE_1, "Pagamento não encontrado", "Se você já pagou, aguarde um pouco e verifique novamente."));
		}
		
		showPurchaseStatusWindow(player, p);
	}
	
	/*
	 * Um Payment foi encontrado, vamos verificar seu status
	 */
	public void handleMPCheck(Purchase p, Payment payment)
	{
		final Player player = World.getInstance().getPlayer(p.getPlayerId());
		
		// https://www.mercadopago.com.br/developers/pt/docs/checkout-api/response-handling/collection-results
		// Poderíamos utilizar o binary_mode?
		switch (payment.getStatus())
		{
			case "in_mediation":
			case "cancelled":
			case "refunded":
			case "charged_back":
			case "rejected":
				p.changeStatus(PurchaseStatus.FAILED);
				if ((player != null) && player.isOnline())
				{
					player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
					showPurchaseStatusWindow(player, p);
				}
				break;
			
			case "pending":
			case "in_process":
			case "authorized":
				// Verificação final da task e nenhum pagamento encontrado, purchase será encerrada
				if (p.getStatus() == PurchaseStatus.FINISHING)
				{
					DonationTaskManager.getInstance().remove(p);
					deleteOrExpire(p);
					return;
				}
				
				// Payment existe, mas o qrcode não estava associado a purchase. Provavelmente uma restart do server
				if ((p.getPaymentMethod() == PaymentMethod.PIX) && (p.getQrCode() == null))
				{
					p.setQrCode(payment.getPointOfInteraction().getTransactionData().getQrCode());
					showPurchaseStatusWindow(player, p);
					showQrCodeWindow(player, p);
					return;
				}
				
				if ((player != null) && player.isOnline())
				{
					p.setMessage(String.format(HTML_MESSAGE_1, "Pagamento não encontrado", "Se você já pagou, aguarde um pouco e verifique novamente."));
					showPurchaseStatusWindow(player, p);
				}
				break;
			
			case "approved":
				// Pode acontecer em caso de concorrência?
				if (p.getStatus() == PurchaseStatus.COMPLETED)
				{
					return;
				}
				
				// É possível que isso aconteça?
				if (payment.getTransactionDetails().getTotalPaidAmount().intValue() != p.getTotalPrice())
				{
					p.changeStatus(PurchaseStatus.FAILED);
					showCustomWindow(player, "exception.htm");
					return;
				}
				
				// Associar a Preference ao Payment
				// O link ainda fica disponível para pagamento, por isso tratamos ele. O mesmo não acontece com o PIX
				if (p.getPaymentMethod() == PaymentMethod.LINK)
				{
					p.setMpPaymentId(payment.getId());
					MercadoPagoAPI.getInstance().closePurchase(p);
				}
				
				onCompletedPayment(player, p);
				break;
		}
	}
	
	public void setEmail(String name, String email)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE accounts SET email=? WHERE login=?");)
		{
			ps.setString(1, email);
			ps.setString(2, name);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String getEmail(String name)
	{
		String email = "";
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT email FROM accounts WHERE login = ?");
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
			{
				return email = rs.getString("email");
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return email;
	}
	
	public static DonationManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DonationManager INSTANCE = new DonationManager();
	}
}