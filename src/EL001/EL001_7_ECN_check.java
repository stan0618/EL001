package EL001;

import java.util.ArrayList;
import java.util.Iterator;

import com.agile.api.APIException;
import com.agile.api.ChangeConstants;
import com.agile.api.IAgileSession;
import com.agile.api.IChange;
import com.agile.api.INode;
import com.agile.api.IRow;
import com.agile.api.ITable;
import com.agile.api.StatusConstants;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;
import com.anselm.plm.utilobj.LogIt;

public class EL001_7_ECN_check implements IEventAction{

	static LogIt log = new LogIt("EL001_7");
	static StringBuilder msg = new StringBuilder();

	public final int Product_Develop_Unit = 1564;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/*
	 * ECN單(non-Javadoc)
	 * @see com.agile.px.IEventAction#doAction(com.agile.api.IAgileSession, com.agile.api.INode, com.agile.px.IEventInfo)
	 * 產品開發單位
	 */

	@Override
	public EventActionResult doAction(IAgileSession arg0, INode arg1, IEventInfo req) {
		msg.delete(0, msg.length());
		IObjectEventInfo info = (IObjectEventInfo) req;
		try {
			//log.setLogFile("C:\\EL001_7.txt");
			IChange change = (IChange) info.getDataObject();
			String event_name = req.getEventName();
			boolean ret = false;
			String status = change.getValue(ChangeConstants.ATT_COVER_PAGE_STATUS).toString().toLowerCase();
			switch(status)
			{
				case "rd create":
					log.log("必填產品開發單位");
					ret = EL001_util.hasValue(change, new int[]{Product_Develop_Unit});
					log.log(2,(ret==true)?"成功":"失敗");
					msg.append(ret?"":"必填欄位: 產品開發單位\r\n");
					ITable table = change.getTable(ChangeConstants.TABLE_RELATIONSHIPS);
					Iterator<?> it = table.iterator();
					ArrayList<String> list = new ArrayList<>();
					while(it.hasNext()){
						IRow row = (IRow) it.next();
						list.add(row.getReferent().getAgileClass().getName());
					}
					if(!list.contains("ECR"))
						ret=false;msg.append("請關聯ECR\r\n");
					break;
				default:
					ret=true;msg.append("非檢查站別");
			}
			if(ret==false)
				throw new Exception();
			log.log("END");
			return new EventActionResult(req, new ActionResult(ActionResult.STRING, "EL001_7程式結束"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return new EventActionResult(req, new ActionResult(ActionResult.EXCEPTION, new Exception(msg.toString())));
		}
	}

}
