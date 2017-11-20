package EL001;

import com.agile.api.APIException;
import com.agile.api.ChangeConstants;
import com.agile.api.IAgileSession;
import com.agile.api.IChange;
import com.agile.api.INode;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;
import com.anselm.plm.utilobj.LogIt;

public class EL001_45_Production_Release_check implements IEventAction{

	/*
	 * 用於
	 * 業務開的PRD單EL001_4
	 * 研發開的PRD單EL001_5
	 */

	//清空欄位與必填欄位
	static final int Product_Develop_Unit = 1543;//(產品開發單位)
	static final int RD_Person_in_charge = 1539;

	static StringBuilder msg = new StringBuilder();
	static LogIt log = new LogIt("EL001_45");

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		IAgileSession session = EL001_util.connect();
//		IChange change;
//		try {
//			change = (IChange) session.getObject(IChange.OBJECT_TYPE, "PRD001");
//
//			boolean ret = EL001_util.cleanValue(change, new int[]{RD_Person_in_charge});
//			System.out.println(ret);
//
//		} catch (APIException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	@Override
	public EventActionResult doAction(IAgileSession arg0, INode arg1, IEventInfo req) {
		// TODO Auto-generated method stub
		boolean ret = false;
		msg.delete(0, msg.length());
		IObjectEventInfo info = (IObjectEventInfo) req;
		try {
			//log.setLogFile("C:\\EL001_45.txt");
			IChange change = (IChange) info.getDataObject();
			String event_name = req.getEventName();
			String status = (String) change.getValue(ChangeConstants.ATT_COVER_PAGE_STATUS).toString().toLowerCase();
			switch(status)
			{
				case "EL001_4_1":
					log.log("清空產品開發單位");
					ret = EL001_util.cleanValue(change, new int[]{Product_Develop_Unit});
					if(ret == false)
						msg.append("唯讀欄位: 產品開發單位");
					break;
				case "request":
					log.log("必填產品開發單位");
					ret = EL001_util.hasValue(change, new int[]{Product_Develop_Unit});
					if(ret == false)
						msg.append("必填欄位: 產品開發單位");
					break;
				case "product assignment":
					log.log("必填RD產品負責人");
					ret = EL001_util.hasValue(change, new int[]{RD_Person_in_charge});
					if(ret == false)
						msg.append("必填欄位: RD Person In Charge (RD產品負責人)");
					break;
				default:
					ret=true;msg.append("非檢查站別");
			}

			System.out.println(ret);
			if(ret == false)
				throw new Exception();
			log.log("END");
			msg.append("END");
			return new EventActionResult(req, new ActionResult(ActionResult.STRING, msg.toString()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new EventActionResult(req, new ActionResult(ActionResult.EXCEPTION, new Exception(msg.toString())));
		}

	}

}
