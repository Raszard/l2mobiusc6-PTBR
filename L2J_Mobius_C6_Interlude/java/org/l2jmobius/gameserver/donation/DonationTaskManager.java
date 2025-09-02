package org.l2jmobius.gameserver.donation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.donation.Purchase.PurchaseStatus;

public class DonationTaskManager implements Runnable
{
	private final Set<Purchase> _list = ConcurrentHashMap.newKeySet();
	
	protected DonationTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 10000, 10000);
	}
	
	public void add(Purchase p)
	{
		_list.add(p);
	}
	
	public void remove(Purchase p)
	{
		_list.remove(p);
	}
	
	@Override
	public void run()
	{
		if (_list.isEmpty())
		{
			return;
		}
		
		for (Purchase purchase : _list)
		{
			// Essa compra não irá expirar mais
			if ((purchase.getStatus() != PurchaseStatus.WAITING) && (purchase.getStatus() != PurchaseStatus.CREATED))
			{
				_list.remove(purchase);
				continue;
			}
			
			if (purchase.timeExpired())
			{
				// Essa compra pode ter sido paga e o player ainda não checou o status dela
				// Não removemos da task porque se sua execução for bloqueada, tentaremos novamente
				if ((purchase.getMpPaymentId() != 0) || (purchase.getMpPreferenceId() != null))
				{
					purchase.changeStatus(PurchaseStatus.FINISHING);
					MercadoPagoAPI.getInstance().checkPurchase(purchase);
					return;
				}
				
				DonationManager.getInstance().deleteOrExpire(purchase);
				_list.remove(purchase);
			}
		}
	}
	
	public static final DonationTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DonationTaskManager INSTANCE = new DonationTaskManager();
	}
}