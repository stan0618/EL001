package EL001;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.agile.api.APIException;
import com.agile.api.ChangeConstants;
import com.agile.api.IAgileList;
import com.agile.api.IAgileSession;
import com.agile.api.ICell;
import com.agile.api.IChange;
import com.agile.api.INode;
import com.agile.api.IRow;
import com.agile.api.ITable;
import com.agile.api.IUser;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;
import com.agile.px.ISaveAsEventInfo;
import com.anselm.plm.utilobj.LogIt;

public class EL001_3_Developing_Request_check implements IEventAction{
	static IAgileSession session = null;
	static String UserName = null;
	static String PWD = null;
	static String LogURL = null;
	static LogIt log = new LogIt("EL001_3");
	static StringBuilder sb =new StringBuilder();
	static  int MODEL_TYPE = 1540 ;
	static HashMap<String, Integer> model_type_map = new HashMap<String, Integer>();

	void initMap()
	{
		model_type_map.put("Developing Request_Ambient Light Sensor", 1542);
		model_type_map.put("Developing Request_Discrete", 1553);
		model_type_map.put("Developing Request_IR", 1540);
		model_type_map.put("Developing Request_IRM", 1540);
		model_type_map.put("Developing Request_ITR", 1540);
		model_type_map.put("Developing Request_Module", 1542);
		model_type_map.put("Developing Request_PD", 1540);
		model_type_map.put("Developing Request_Photo Coupler",1542 );
		model_type_map.put("Developing Request_Photolink", 1540);
		model_type_map.put("Developing Request_PT", 1540);
		model_type_map.put("Developing Request_RGB color sensor", 1543);
		model_type_map.put("Developing Request_Visible", 1541);
		model_type_map.put("Developing Request_Visible-AM", 1539);
		model_type_map.put("Developing Request_Visible-High Power", 1539);
	}
	/*
	 * EL001_3_2
	 * 到Request站別前檢查以下欄位是否有值
	 * 業務需求日期 1534//2002
	 * 預計接單日期 1535/2003
	 * 產品開發單位 1575//1540
	 * 分4個case處理
	 * RoHS Requirements 1539//2022
	 * Halogen Free 1540//1277
	 * RoHS客戶佐證資料附件名稱 1576//N1303
	 * 無鹵素要求客戶佐證資料附件名稱 1577//N1304
	 * attachment type 3651//4681
	 * attachment.filename 1046//1046
	 */
	static boolean EL001_3_2(IChange change)
	{
		boolean ret = true;
		log.log(1,"開始執行EL001_3_2");
		int baseID[] = new int[]{2002,2003,MODEL_TYPE};
		try {
			for(int i = 0; i < baseID.length; i++)
			{
				ICell c = (ICell) change.getCell(baseID[i]);
				log.log(2,"檢查'"+c.getName()+"'是否有值");
				if(c.getValue() == null||c.getValue().toString().isEmpty())
				{
					log.log(3,"無值");
					sb.append(change.getCell(baseID[i]).getName()+": 無值\r");
					ret= false;
				}else{
					log.log(3,"有值");
				}
				//System.out.println(c.getName());
			}

			/*
			 * RoHS Requirements == Customer Standard and
			 * Halogen Free == No
			 */
			if(change.getCell(2022).getValue().toString().toLowerCase().contains("customer standard") && change.getCell(1277).getValue().toString().contains("No"))
			{
				int[] baseIDCase1 = new int[]{1303,1304};
				log.log(2,"進入case 1: RoHS Requirements == Customer Standard and Halogen Free == No");
				ITable attachmentTable = change.getAttachments();
				Iterator it =  attachmentTable.getTableIterator();
				int[] has_type = new int[]{0,0};
				while(it.hasNext())
				{
					IRow row = (IRow) it.next();
					for(int i = 0; i < baseIDCase1.length; i++)
					{
						//log.log(row.getCell(4681).getValue().toString());
						IAgileList[] selected =  (IAgileList[]) ((IAgileList) row.getCell(4681).getValue()).getSelection();

						if(selected.length != 0)
						{
							switch(selected[0].getAPIName())
							{
								case "1":
									has_type[0] = 1;
									break;
								case "2":
									has_type[1] = 1;
									break;
							}
						}
					}
				}

				if(has_type[0]+has_type[1] != 2)
				{
					log.log(2,"沒有上傳兩筆attachments");
					sb.append("沒有上傳兩種attachments\r");
					ret= false;
				}

				int[] baseIDCase11 = new int[]{1303,1304};
				for(int i = 0; i < baseIDCase11.length; i++)
				{
					log.log(2,"檢查'"+change.getCell(baseIDCase1[i])+"'是否有值");
					if(change.getCell(baseIDCase1[i]).getValue() == null)
					{
						log.log(3,"無值");
						sb.append(change.getCell(baseID[i]).getName()+": 無值\r");
						ret= false;
					}
					log.log(3,"有值");
				}
			}
			/*
			 * RoHS Requirements == Customer Standard and
			 * Halogen Free == Yes
			 */
			else if(change.getCell(2022).getValue().toString().toLowerCase().contains("customer standard") && change.getCell(1277).getValue().toString().contains("Yes"))
			{
				log.log(2,"進入case 2: RoHS Requirements == Customer Standard and Halogen Free == Yes");
				ITable attachmentTable = change.getAttachments();
				Iterator it =  attachmentTable.getTableIterator();

				int has_type = 0;
				while(it.hasNext())
				{
					IRow row = (IRow) it.next();
					IAgileList[] selected = (IAgileList[])((IAgileList) row.getCell(4681).getValue()).getSelection();
					log.log(2,"檢查'"+row.getCell(1046).getValue()+"'的"+row.getCell(4681).getName()+"'是否為RoHS客戶佐證資料");

					if(!row.getCell(4681).getValue().toString().contains("RoHS客戶佐證資料"))
					{
						log.log(3,"不同");
					}
					else
					{
						has_type = 1;
						log.log(3,"相同");
					}
				}
				if(has_type != 1 )
				{
					log.log(2,"沒有上傳RoHS客戶佐證資料的attachments");
					sb.append("沒有上傳RoHS客戶佐證資料的attachments\r");
					ret= false;
				}
				log.log(2,"檢查'"+change.getCell(1303).getName()+"'是否有值");
				if(change.getCell(1303).getValue() == null)
				{
					log.log(3,"無值");
					sb.append(change.getCell(1303).getName()+": 無值\r");
					ret= false;
				}
				log.log(3,"有值");
			}
			/*
			 * RoHS Requirements == Everlight Standard and
			 * Halogen Free == No
			 */
			else if(change.getCell(2022).getValue().toString().toLowerCase().contains("everlight standard") && change.getCell(1277).getValue().toString().contains("No"))
			{
				log.log(2,"進入case 3: RoHS Requirements == Everlight Standard and Halogen Free == No");
				ITable attachmentTable = change.getAttachments();
				Iterator it =  attachmentTable.getTableIterator();

				int has_type = 0;
				while(it.hasNext())
				{
					IRow row = (IRow) it.next();
					IAgileList[] selected = (IAgileList[])((IAgileList) row.getCell(4681).getValue()).getSelection();
					log.log(2,"檢查'"+row.getCell(1046).getValue()+"'的"+row.getCell(4681).getName()+"'是否為無鹵素要求客戶佐證資料");

					if(!row.getCell(4681).getValue().toString().contains("無鹵素要求客戶佐證資料"))
					{
						log.log(3,"不同");
					}
					else
					{
						has_type = 1;
						log.log(3,"相同");
					}
				}
				if(has_type != 1 )
				{
					log.log(2,"沒有上傳無鹵素要求客戶佐證資料的attachments");
					sb.append("沒有上傳無鹵素要求客戶佐證資料的attachments\r");
					ret= false;
				}
				log.log(2,"檢查'"+change.getCell(1304).getName()+"'是否有值");
				if(change.getCell(1304).getValue() == null)
				{
					log.log(3,"無值");
					sb.append(change.getCell(1304).getName()+": 無值\r");
					ret=false;
				}
				log.log(3,"有值");
			}
			/*
			 * RoHS Requirements == Everlight Standard and
			 * Halogen Free == Yes
			 */
			else
			{
				log.log(2,"進入case 4: RoHS Requirements ==  Everlight Standard and Halogen Free == Yes");
				log.log(2,"不檢查");
				//不檢查
			}

			Date current = new Date();
			log.log(2,"判斷'"+change.getCell(2003).getName()+"'與'"+change.getCell(2002).getName()+"'的日期是否晚於表單送出日");
			if((current).after((Date) change.getCell(2003).getValue()) && current.after((Date) change.getCell(2002).getValue()))
			{
				log.log(3,"否");
				sb.append(change.getCell(2003).getName()+"'與'"+change.getCell(2002).getName()+"'的日期早於表單送出日");
				ret= false;
			}else{
				log.log(3,"是");
			}

		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret= false;
		}

		return ret;
	}

	/*
	 * EL001_3_3
	 * 檢查欄位是否有值
	 * RD Developer 1564//2025
	 */
	static boolean EL001_3_3(IChange change)
	{
		log.log(1,"開始執行EL001_3_3");
		try {
			log.log(2,"檢查'"+change.getCell(2025).getName()+"'是否有值");
			IAgileList[] selected = (IAgileList[])((IAgileList) change.getCell(2025).getValue()).getSelection();

			if(selected.length == 0)
			{
				log.log(3,"無值");
				sb.append(change.getCell(2025).getName()+": 無值\r");
				return false;
			}
			log.log(3,"有值");
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 * EL001_3_4
	 * 檢查是否有值
	 * Whether Trial Run 1541//1280
	 * RD Comment or Trail Run Schedule 1578//1331
	 * Development Suggestions 2000003297//1334
	 */
	static boolean EL001_3_4(IChange change)
	{
		boolean ret = true;
		String str = " ";
		int[] baseID = new int[]{1280,1331,1334};
		log.log(1,"開始執行EL001_3_4");
		try {
			IAgileList[] selected = (IAgileList[])((IAgileList) change.getCell(1280).getValue()).getSelection();
			for(int i = 0; i < baseID.length; i++)
			{
				str = str +" '" +change.getCell(baseID[i]).getName()+"'";
			}
			log.log(2,"檢查"+str+"是否全部有值");
			if(selected.length == 0)
			{
				log.log(3,"否");
				sb.append(change.getCell(1280).getName()+": 無值\r");
				ret= false;
			}
			if(change.getCell(1331).getValue().toString().isEmpty())
			{
				log.log(3,"否");
				sb.append(change.getCell(1331).getName()+": 無值\r");
				ret = false;
			}
			if(change.getCell(1334).getValue().toString().isEmpty())
			{
				log.log(3,"否");
				sb.append(change.getCell(1334).getName()+": 無值\r");
				ret = false;
			}
			if(ret){
				log.log(3,"是");
				return true;
			}

			return ret;

		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}


	}

	/*
	 * EL001_3_5
	 * 檢查是否有值
	 * 已收到樣品  1542//1274
	 * 已收到規格書 1543//1275
	 * 已收到其他報告 1544//1276
	 * 其他驗收事項  1579//2018
	 * DR Status 1545//2026
	 * Customer Feedback 1580//1333
	 * PM and Marketing Confirmation 1546 //2019
	 */
	static boolean EL001_3_5(IChange change)
	{
		log.log(1,"開始執行EL001_3_5");
		int[] baseID = new int[]{1274,1275,1276,2018,2026,1333,2019};
		boolean ret = true;
		for(int id : baseID)
		{
			ret = EL001_util.hasValue(change, new int[]{id});
			if(ret == false){
				try {
					sb.append("必填欄位: "+change.getCell(id).getName()+(": 無值\r"));
				} catch (APIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				return ret;
			}

//			try {
//				//sb.append(change.getCell(1334).getName()+(ret?"有值\r":" 無值\r"));
//
//			} catch (APIException e) {
//				// TODO Auto-generated catch block
//				sb.append(e);
//				 return false;
//			}
		}
		return ret;

//			for(int i = 0; i < baseID.length; i++)
//			{
//				IAgileList[] selected = (IAgileList[])((IAgileList) change.getCell(baseID[i]).getValue()).getSelection();
//				log.log(2,"檢查'"+change.getCell(baseID[i]).getName()+"'是否有值");
//				if(selected.length == 0)
//				{
//					log.log(3,"無值");
//					return false;
//				}
//				log.log(3,"有值");
//			}
//			log.log(2,"檢查'"+change.getCell(2018).getName()+"與'"+change.getCell(1333).getName()+"'是否有值");
//			if(change.getCell(2018).getValue().toString().isEmpty() || change.getCell(1333).getValue().toString().isEmpty())
//			{
//				log.log(3,"無值");
//				return false;
//			}
//			log.log(3,"有值");

	}

	@Override
	public EventActionResult doAction(IAgileSession arg0, INode arg1, IEventInfo req)
	{

		log.log("開始執行EL001");
		sb.delete(0, sb.length());
		//session = EL001_util.connect();
		boolean ret = true;
		initMap();
		try
		{
//			log.setLogFile("C:\\Users\\anselm\\peter.txt");
			IObjectEventInfo info = (IObjectEventInfo) req;
			IChange change = (IChange) info.getDataObject();
			String event_name = req.getEventName();
			String status = change.getValue(ChangeConstants.ATT_COVER_PAGE_STATUS).toString().toLowerCase();
			if(!change.getValue(1069).toString().toLowerCase().contains("developing request"))
			{
				log.log(change.getValue(1069).toString().toLowerCase());
				return new EventActionResult(req, new ActionResult(ActionResult.STRING, "非developing request"));
			}
			if(model_type_map.get(change.getAgileClass().getName())!=0)
			{
				MODEL_TYPE=model_type_map.get(change.getAgileClass().getName());
			}
			//判斷事件類型，並執行動作
			switch(status)
			{

				case "request":
					ret = EL001_3_2(change);
					break;
				case "request preview by rd":
					ret = EL001_3_3(change);
					break;
				case "rd develop":
					ret = EL001_3_4(change);
					break;
				case "customer feedback":
					ret = EL001_3_5(change);
					break;
				default:
					ret=true;sb.append("非檢查站別");
			}
			if(ret == false){
				throw new Exception();
			}
			log.log("END");
			return new EventActionResult(req, new ActionResult(ActionResult.STRING, "EL001_3程式結束"));
		}
		catch (Exception e)
		{
			log.log("ERROR");
			// TODO Auto-generated catch block
			return new EventActionResult(req, new ActionResult(ActionResult.EXCEPTION, new Exception(sb.toString())));
		}


	}
}