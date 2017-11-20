package EL001;

import java.util.ArrayList;
import java.util.Iterator;

import com.agile.api.APIException;
import com.agile.api.ChangeConstants;
import com.agile.api.IAgileList;
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
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;
import com.anselm.plm.utilobj.LogIt;

/*
 * EL001_6
 * ECR單(內部委託使用)
 */
public class EL001_6_ECR_check implements IEventAction{

	static StringBuilder msg = new StringBuilder();
	static LogIt log = new LogIt("EL001_5n6");
	/*
	 * 產品開發單位
	 *
	 * For EL001_6_3
	 * 驗證人員
	 * 億光需發行PCN/PTN
	 *
	 * For EL001_6_4
	 * 變更原因
	 * Reliability Test NO
	 *
	 * For EL001_6_5
	 * 庫存品處理方式
	 * 庫存處理說明
	 * 億光需發行PCN/PTNF
	 * Attachment Type 3651
	 *
	 */

	static final int Reason_Code = 1543;//1049
	static final int Product_Develop_Unit = 1564;//產品開發單位
	static final int Verification_Staff =1566;//驗證人員
	static final int PCN_PTN = 1542;//億光需發行PCN/PTN
	static final int Change_Reason = 1543;//變更原因
	static final int Reliability_Test_NO = 1567;//信賴式實驗編號
	static final int Inventory_Processing = 1545;//庫存品處理方式
	static final int Inventory_Processing_Description = 1568;//庫存品處理說明
	static final int Attachment_Type = 4681;
	static final int Number_Pause = 1546 ;//料號是否暫停下單

	//	public static void main(String[] args)
//	{
//		IAgileSession session = EL001_util.connect();
//		IChange change;
//		try {
//			change = (IChange) session.getObject(IChange.OBJECT_TYPE, "ECR001");
//			//System.out.println(EL001_util.isValue(change, new int[]{Reason_Code}, new String[]{"01"}));
//			boolean ret = EL001_6_2(change);
//			System.out.println(ret);
//
//		} catch (APIException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	private  IUser[] getApprovers(IAgileList userList) throws Exception {
		IAgileList[] selected = userList.getSelection();
		IUser[] approvers = new IUser[selected.length];
		for(int index=0;index<selected.length;index++)    approvers[index] = (IUser) selected[index].getValue();
		return approvers;
	}

	@Override
	public EventActionResult doAction(IAgileSession arg0, INode arg1, IEventInfo req)
	{
		msg.delete(0, msg.length());
		IObjectEventInfo info = (IObjectEventInfo) req;
		try {
			//log.setLogFile("C:\\EL001_6.txt");
			IChange change = (IChange) info.getDataObject();
			String event_name = req.getEventName();
			boolean ret = false;
			String status = change.getValue(ChangeConstants.ATT_COVER_PAGE_STATUS).toString().toLowerCase();
			switch(status)
			{
				case "EL001_6_1":
					log.log("清空產品開發單位");
					ret = EL001_util.cleanValue(change, new int[]{Product_Develop_Unit});
					log.log(2,(ret==true)?"成功":"失敗");
					msg.append((ret==true)?"":"必填欄位: 產品開發單位 | ");
					break;
				case "request":
					ret = EL001_6_2(change);
					break;
				case "related depts. review":
					IUser orginator=(IUser) change.getCell(ChangeConstants.ATT_COVER_PAGE_ORIGINATOR).getReferent();
					IUser loginuser = arg0.getCurrentUser();
					if(loginuser.equals(orginator)){
						log.log("必填驗證人員、億光需發行PCN/PTN");
						ret = EL001_util.hasValue(change, new int[]{Verification_Staff, PCN_PTN});
						log.log(2,(ret==true)?"成功":"失敗");
						msg.append((ret==true)?"":"必填欄位: 驗證人員、億光需發行PCN/PTN");
					}else{
						ret=true;
					}
					break;
				case "validation":
					boolean wflag=true;
					log.log("變更原因=01/05/06時，Reliability_Test_NO須以'准'或'委'字開頭或'不需RA'");
					if(EL001_util.hasValue(change, new int[]{Reason_Code}, new String[]{"01"}) || EL001_util.hasValue(change, new int[]{Reason_Code}, new String[]{"05"}) ||
							EL001_util.hasValue(change, new int[]{Reason_Code}, new String[]{"06"}))
					{
						log.log(change.getCell(Reliability_Test_NO).getValue().toString());
						if(change.getCell(Reliability_Test_NO).getValue().toString().startsWith("委-")){
							ret = true;
						}else if(change.getCell(Reliability_Test_NO).getValue().toString().startsWith("准-")){
							ret = true;
						}else if(change.getCell(Reliability_Test_NO).getValue().toString().startsWith("不需RA")){
							ret = true;
						}else if(change.getCell(Reliability_Test_NO).getValue().toString().startsWith("不須RA")){
							ret=false;
							wflag=false;
						}else{
							ret=false;
						}
						if(ret == false)
							msg.append("Reason Code(變更原因)=01/05/06時，Reliability_Test_NO須以'准-'或'委-'字開頭或'不需RA'");
						if(wflag==false)
							msg.append("請將「不須RA」修改為「不需RA」");
						log.log(2,(ret==true)?"成功":"失敗");
					}else{
						ret = true;
					}

					break;
				case "validation review":
					IUser orginator1 = (IUser) change.getCell(ChangeConstants.ATT_COVER_PAGE_ORIGINATOR).getReferent();
					IUser loginuser1 = arg0.getCurrentUser();
					if (loginuser1.equals(orginator1)) {
						log.log("必填庫存品處理方式、庫存處理說明、億光需發行PCN/PTN");
						ret = EL001_util.hasValue(change,
								new int[] { Inventory_Processing_Description, Inventory_Processing, PCN_PTN });
						if (ret == false)
							msg.append("必填欄位: 庫存品處理方式、庫存處理說明、億光需發行PCN/PTN");
						if (EL001_util.isValue(change, new int[] { PCN_PTN }, new String[] { "Yes" })) {
							log.log(2, "億光需發行PCN/PTN=Yes");
							int has_type = 0;
							ITable table = change.getAttachments();
							Iterator it = table.iterator();
							log.log(2, "必填attachment");
							while (it.hasNext()) {
								IRow row = (IRow) it.next();
								if(row.getCell(Attachment_Type).getValue().toString().contains("PCN")){
									ret = true;
									has_type = 1;
								}
							}
							log.log(2, (has_type == 1) ? "是" : "否");
							if (has_type == 0) {
								msg.append("需上傳Attachment Type為PCN的附件");
								ret = false;
							}
						}
					}else{
						ret=true;
					}
					break;
				default:
					ret=true;msg.append("非檢查站別");
			}
			if(ret == false)
				throw new Exception();

			System.out.println(ret);
			msg.append("END");
			return new EventActionResult(req, new ActionResult(ActionResult.STRING, msg.toString()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return new EventActionResult(req, new ActionResult(ActionResult.EXCEPTION, new Exception(msg.toString())));
		}

	}

	static boolean EL001_6_2(IChange change)
	{
		boolean ret = true;
		//檢查產品開發單位
		log.log("必填產品開發單位");
		if(!EL001_util.hasValue(change, new int[]{Product_Develop_Unit}))
		{
			log.log(2,"否");
			msg.append("必填欄位: 產品開發單位 | ");
			ret=false;
		}
		try {
			//檢查有沒有affected items
			log.log("必填affected items");
			ITable affected = change.getTable(ChangeConstants.TABLE_AFFECTEDITEMS);
			if(affected.size() == 0)
			{
				log.log(2,"否");
				msg.append("必填欄位: Affected Items | ");
				ret= false;
			}
			else
			{
				Iterator it = affected.iterator();
				//檢查料號有沒有temp
				log.log(2,"是");
				log.log(2,"料號不包含temp");
				while(it.hasNext())
				{
					IRow row = (IRow) it.next();
					if(row.getReferent().getName().toLowerCase().contains("temp"))
					{
						log.log(3,row.getReferent().getName()+": 否");
						msg.append("料號不能包含temp字詞 | ");
						ret= false;
					}
					//System.out.println(row.getReferent().getName().toLowerCase().contains("d"));
				}
			}

			ITable attachtments = change.getAttachments();
			Iterator it = attachtments.iterator();

			int has_type = 0;
			log.log("檢查attachments");
			ArrayList<String> documentList = new ArrayList<String>();
			documentList.add("客戶文件(Customer Document/Mail)");
			documentList.add("技術文件(Technical Document)");
			documentList.add("供應商文件(Vendor Document/Mail)");
			documentList.add("簽呈文件(Signed Document)");
			documentList.add("信賴性失效報告(RA Failure Report File)");
			documentList.add("失效分析報告(RA Analysis Report File)");
			if(EL001_util.hasValue(change, new int[]{Reason_Code }, new String[]{"01"}) || EL001_util.hasValue(change, new int[]{Reason_Code }, new String[]{"03"}) ||
					EL001_util.hasValue(change, new int[]{Reason_Code }, new String[]{"04"}) || EL001_util.hasValue(change, new int[]{Reason_Code }, new String[]{"05"}) ||
					EL001_util.hasValue(change, new int[]{Reason_Code }, new String[]{"06"}))
			{
				log.log("Case 1: Reason Code = 01/03/04/05/06");
				while(it.hasNext())
				{
					has_type = 1;
					IRow row = (IRow) it.next();
					if(!documentList.contains(row.getCell(Attachment_Type).getValue().toString()))
					{
						msg.append("不可上傳文件類型的Attachment | ");
						ret=false;
					}
				}
				log.log(1,"必填:　attachments");
				log.log(2,(has_type==1)?"是":"否");
				if(has_type == 0)
				{
					msg.append("必填欄位: Attachments");
					ret= false;
				}
			}
			else if(EL001_util.hasValue(change, new int[]{Reason_Code}, new String[]{"07"}))
			{
				log.log("Case 3: Reason Code = 07");
				log.log(1,"必填: 料號是否暫停下單");
				if(!EL001_util.hasValue(change, new int[]{Number_Pause}))
				{
					log.log(2,"否");
					msg.append("必填欄位: 料號是否暫停下單 | ");
					ret=false;
				}

				//找attachment有沒有信賴性失效報告與失效分析報告

				else if(EL001_util.hasValue(change, new int[]{Number_Pause}, new String[]{"01"}))
				{
					log.log(2,"是");
					log.log(2,"料號是否暫停下單=01");
					log.log(2,"attachment中必填信賴性失效報告");
					while(it.hasNext())
					{
						IRow row = (IRow) it.next();
						if(row.getCell(Attachment_Type).getValue().toString().contains("信賴性失效報告"))
							has_type = 1;
					}
					log.log(3,has_type==1?"是":"否");
					if(has_type == 0)
					{
						msg.append("必填欄位: 信賴性失效報告 | ");
						ret= false;
					}
				}
				else if(EL001_util.hasValue(change, new int[]{Number_Pause}, new String[]{"02"}))
				{
					log.log(2,"是");
					log.log(2,"料號是否暫停下單=02");
					log.log(2,"attachment中必填失效分析報告");
					while(it.hasNext())
					{
						IRow row = (IRow) it.next();
						if(row.getCell(Attachment_Type).getValue().toString().contains("失效分析報告"))
							has_type = 1;
					}
					log.log(3,has_type==1?"是":"否");
					if(has_type == 0)
					{
						msg.append("必填欄位: 失效分析報告 | ");
						ret=false;
					}
				}
			}


		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			msg.append("致命錯誤: 請通知IT人員 ");
			return false;
		}

		return ret;
	}

}
