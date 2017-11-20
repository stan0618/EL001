package EL002;

import java.util.ArrayList;
import java.util.Iterator;

import com.agile.api.APIException;
import com.agile.api.ChangeConstants;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IChange;
import com.agile.api.IDataObject;
import com.agile.api.IItem;
import com.agile.api.INode;
import com.agile.api.IRow;
import com.agile.api.IStatus;
import com.agile.api.ITable;
import com.agile.api.IUser;
import com.agile.api.ItemConstants;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.ICustomAction;
import com.anselm.plm.utilobj.LogIt;

import EL001.EL001_util;

public class EL002_2_BOM_Check_px_rollback implements ICustomAction {

	static IAgileSession adminsession = null;
	static StringBuilder msg = new StringBuilder();
	static LogIt log = new LogIt("EL002_1");
	//static IAgileSession session = EL002_util.connect();
	//affected items
	static final int Lifecycle = 1084;//1057;//1084;
	static final int Affected_Name = 1054;//system default
	//item BOM
	static final int Usage =2177;// 2175;
	static final int Alternative_Group = 2175;//1358
	static final int Priority = 2176;//1359;
	static final int Find_Num = 1012;//system default
	static final int Quantity = 1035;//1360;ok
	static final int Basic_Dosage = 2022;//1362;//基礎單位
	static final int Measurement_Unit = 2023;//1361;//計量單位
	static ArrayList<String> documentList = new ArrayList<String>();

	public ActionResult doAction(IAgileSession session, INode actionNode, IDataObject affectedObject) {
		IChange changeAdmin=null;
		IStatus RollBackStatus = null;
		msg.delete(0, msg.length());
		try {
			boolean ret = false;
			IChange change = (IChange) affectedObject;
			adminsession = EL002_util.connect();
			changeAdmin = (IChange) adminsession.getObject(ChangeConstants.CLASS_CHANGE_BASE_CLASS,
					change.getName());
			IAdmin m_admin = session.getAdminInstance();
			IAgileClass DocumentClass = m_admin.getAgileClass(ItemConstants.CLASS_DOCUMENTS_CLASS);
			IAgileClass[] subclasses = DocumentClass.getSubclasses();
			for (int i = 0; i < subclasses.length; ++i) {
				documentList.add(subclasses[i].toString());
			}
			if(change.getAgileClass().getName().equals("ECN")){
				IStatus[] statusList = change.getWorkflow().getStates();
				for (int index = 0; index < statusList.length; index++) {
					if (statusList[index].getName().toLowerCase().equals("rd create")) {
						RollBackStatus = statusList[index];
						break;
					}
				}
			}else{
				IStatus[] statusList = change.getWorkflow().getStates();
				for (int index = 0; index < statusList.length; index++) {
					if (statusList[index].getName().toLowerCase().equals("build bom")) {
						RollBackStatus = statusList[index];
						break;
					}
				}
			}
			ret = EL002(change);
			if(ret == false)
				throw new Exception();
			IUser dummyuser = (IUser) adminsession.getObject(IUser.OBJECT_TYPE, "admin01");
			IUser[] dummyapprovers = new IUser[] { dummyuser };
			changeAdmin.removeApprovers(changeAdmin.getStatus(), dummyapprovers, null,"Remove dummy user from approvers");
			return new ActionResult(ActionResult.STRING, "程式執行結束");
		} catch (Exception e) {
			try {
				changeAdmin.setValue(1053, msg.toString());
			} catch (APIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return new ActionResult(ActionResult.EXCEPTION, new Exception("SET VALUE FAIL"));
			}
			try {
				if(!changeAdmin.getValue(1053).toString().isEmpty())
					changeAdmin.changeStatus(RollBackStatus, false, "BOM ERROR ROLLBACK" , false, false, null, null,
							null, false);
			} catch (APIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return new ActionResult(ActionResult.EXCEPTION, new Exception("ROLLBACK FAIL") );
			}
			return new ActionResult(ActionResult.EXCEPTION, new Exception("BOM ERROR ROLLBACK") );
		}
	}

	static boolean EL002(IChange change)
	{
		boolean ret = true;
		try{
			//affected items
			IItem[] items = EL002_util.getAffectedItems(change);
			if(items.length == 0)
			{
				log.log("錯誤: 沒有Affected items");
				msg.append("錯誤: 沒有Affected items\r\n");
				return false;
			}

			ITable afftable = change.getTable(ChangeConstants.TABLE_AFFECTEDITEMS);
			Iterator it = afftable.iterator();
			while(it.hasNext()){
				IRow row =(IRow) it.next();
				IItem item = (IItem) row.getReferent();
				if(!change.getAgileClass().getName().contains("ECN"))
				{
					//EL001_1hasTypeInRelation
					log.log("檢查Affected items的Lifecycle");//1
					log.log(row.getCell(4500).getValue().toString());
					if(row.getCell(4500).getValue().toString().equals("Approved(正式核准)"))
					{
						log.log(1,item.getName()+"不能為Approved物件");
						msg.append("["+item.getName()+"]不能為Approved(正式核准)\r\n");
						ret=false;
					}
					else
						log.log(1,row.getName()+": 正確");

					log.log("檢查料號是否為成品");//2
					if(item.getAgileClass().getName().toLowerCase().startsWith("f"))
					{
						log.log(1,"是。並檢查是否有製造規格");
						boolean re0 = EL002_util.hasTypeInRelation(item, "manufacturing specifications");
						if(re0 == false)
						{
							log.log(1,"否");
							msg.append("["+item.getName()+"]沒有製造規格\r\n");
							ret=false;
						}
						else
							log.log(1,"是");
					}
					else
						log.log(1,"否");
				}
			}

			for(IItem item : items)
			{
				//8

				log.log("檢查單位需要一致");
				if(item.getCell(Measurement_Unit).toString().isEmpty()||item.getCell(Basic_Dosage).toString().isEmpty())
				{
					log.log(1,"錯誤");
					msg.append("["+item.getName()+"]的"+item.getCell(Measurement_Unit).getName()+"與"+item.getCell(Basic_Dosage).getName()+"不可為空\r\n");
					ret=false;
				}else if(!item.getCell(Measurement_Unit).toString().trim().equals(item.getCell(Basic_Dosage).toString().trim())){
					log.log(1,"錯誤");
					msg.append("["+item.getName()+"]的"+item.getCell(Measurement_Unit).getName()+"與"+item.getCell(Basic_Dosage).getName()+"需要一致\r\n");
					ret=false;
				}else
					log.log(1,"正確");

				//bom以下
				IRow[] BOMitems = EL002_util.getBOMItems(item,change);
				log.log((BOMitems.length==0)?"BOM表不可有文件或是為空":"開始檢查BOM item");//5

				//ArrayList<String> group = new ArrayList<String>();

				for(IRow bom : BOMitems)
				{
					log.log(1,"檢查BOM items的是否為文件");//5
					if(bom.getReferent().getAgileClass().getName().toString().toLowerCase().startsWith("d") || documentList.contains(bom.getCell(3582).getValue().toString()))
					{
						log.log(2,bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"不能為文件\r\n");
						msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]不能為文件\r\n");
						ret=false;
					}else
						log.log(2,bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+": 正確");

					log.log(1,"檢查BOM items的Lifecycle");//3
					if(!bom.getCell(10014).getValue().toString().contains("Approved"))//Approved要改
					{
						log.log(2,bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"不能為未核准料號");
						msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]不能為未核准料號\r\n");
						ret=false;
					}
					else
						log.log(2,bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+": 正確");


					//group.add(bom.getCell(Alternative_Group).getValue().toString());
					//4n9

					if(bom.getValue(Usage) != null)
					{
						log.log("檢查使用比例不可為小數");
						if(bom.getValue(Usage).toString().contains("."))
						{
							log.log(1,bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+": 不能為小數: "+bom.getCell(Usage));
							msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]"+bom.getCell(Usage).getName()+": 不能為小數: "+bom.getCell(Usage)+"\r\n");
							ret=false;
						}else
							log.log(1,bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+": 正確");
					}
					log.log("檢查順位為1的使用比例不可為0");
					if(bom.getValue(Priority).toString().trim().equals("1") )
					{
						if( bom.getCell(Usage) == null||bom.getCell(Usage).toString().matches("0+.?0?") )
						{
							log.log(1,bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+": 錯誤的使用比例: "+bom.getCell(Usage));
							msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]"+bom.getCell(Usage).getName()+": 錯誤的使用比例: "+bom.getCell(Usage)+"\r\n");
							ret=false;
						}
						else
							log.log(1,bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+": 正確");
					}

					log.log("檢查Alternative Group");
					if(bom.getCell(Alternative_Group).toString().isEmpty())
					{
						log.log(1,"Alternative Group為空");
						if(!bom.getCell(Usage).toString().isEmpty()||!bom.getCell(Priority).toString().isEmpty())
						{
							log.log(2,"使用比例與優先權必須都為空");
							msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]的"+bom.getCell(Usage).getName()+"與"+bom.getCell(Priority).getName()+"必須都為空\r\n");
							ret=false;
						}
						else
							log.log(2,"正確");

					}
					else
					{
						log.log(1,"Alternative Group不為空");
						if(bom.getCell(Usage).toString().isEmpty()||bom.getCell(Priority).toString().isEmpty())
						{
							log.log(2,"使用比例與優先權必須都不為空");
							msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]的"+bom.getCell(Usage).getName()+"與"+bom.getCell(Priority).getName()+"必須都不為空\r\n");
							ret=false;
						}
						else
							log.log(2,"正確");
					}
					//6
					log.log("檢查BOM表Find Number格式必須為四位數");
					if(bom.getCell(Find_Num).toString().length() != 4 && bom.getCell(Find_Num).toString().matches("[0-9]+") || bom.getCell(Find_Num).toString().isEmpty())
					{
						log.log(1,bom.getName()+": 錯誤");
						msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]"+bom.getCell(Find_Num).getName()+": 格式必須為四位數\r\n");
						ret=false;
					}else if(!bom.getCell(Find_Num).toString().matches("[0-9]+")){
						log.log(1,bom.getName()+": 錯誤");
						msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]"+bom.getCell(Find_Num).getName()+": 格式必須為數字\r\n");
						ret=false;
					}else
						log.log(1,"正確");
					//7
					log.log("檢查BOM表Quantity不可為0且小數點後不超過三位數");
					String str = bom.getCell(Quantity).getValue().toString();
					if(str.equals("0") || !str.matches("^[0-9]+(.[0-9]{1,3})?$") || str.isEmpty()||str.split(".").length>2)
					{
						log.log(1,"錯誤");
						msg.append("["+item.getName()+"]下階["+bom.getCell(ItemConstants.ATT_BOM_ITEM_NUMBER)+"]的"+bom.getCell(Quantity).getName()+": 不可含有空白或多個小數點，也不可為0且小數點後不超過三位數\r\n");
						ret=false;
					}
					else
						log.log(1,"正確");
					//8
					/*
					log.log("檢查單位需要一至");
					if(!bom.getCell(Measurement_Unit).equals(bom.getCell(Basic_Dosage)) || bom.getCell(Measurement_Unit).toString().isEmpty()||bom.getCell(Basic_Dosage).toString().isEmpty())
					{
						log.log(1,"錯誤");
						return false;
					}
					else
						log.log(1,"正確");
						*/
				}

			}
			return ret;
		}
		catch(Exception ex)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(ex.toString()).append("\r\n");
			for (StackTraceElement elem : ex.getStackTrace()) {
				sb.append("\tat ").append(elem).append("\r\n");
			}
			log.log(sb.toString()+"\r\n");
			return false;
		}

	}


}
