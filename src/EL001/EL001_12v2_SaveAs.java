package EL001;

import java.util.ArrayList;
import java.util.HashMap;

import com.agile.api.APIException;
import com.agile.api.IAgileList;
import com.agile.api.IAgileSession;
import com.agile.api.ICell;
import com.agile.api.IChange;
import com.agile.api.INode;
import com.agile.px.ActionResult;
import com.agile.px.EventActionResult;
import com.agile.px.IEventAction;
import com.agile.px.IEventInfo;
import com.agile.px.IObjectEventInfo;
import com.agile.px.ISaveAsEventInfo;
import com.anselm.plm.utilobj.LogIt;

public class EL001_12v2_SaveAs implements IEventAction{

	static LogIt log = new LogIt("EL001_1n2");
	static String[] str = new String[]{"Technical Re-Engineering Documents Release","New Parts Request","Parts Approval","Data Sheet Release Form ( Data Sheet 文件申請單 )","Document Obsolete (  文件作廢申請單 )","Drawing Document Release ( 圖面文件申請單 )","ECN","Development Documents Request","Internal Production Release","Manuf Spec Release ( 製規申請單 )","Product Document Release ( 產品文件申請單 )","Production Release","ECR"};
	static StringBuilder msg = new StringBuilder();

	/*
	 * model type hash map
	 */

	static HashMap<String, Integer> model_type_map = new HashMap<String, Integer>();

	/*
	 * 欄位與baseID設定
	 */
	static final int MODEL_TYPE =1540 ;
	static final int PDF_Convert_Status = 1060;
	static final int Require_date = 2002;//業務需求日期
	static final int Get_date = 2003;//預計接單日期
	static final int Product_Development_Unit = 1564;//產品開發單位

	//	public static void main(String[] arg)
//	{
//		IAgileSession session = EL001_util.connect();
//		IChange change;
//		boolean ret = false;
//		try {
//			change = (IChange) session.getObject(IChange.OBJECT_TYPE, "EL3_018");
//			//ret = EL001_util.cleanValue(change, new int[]{2000003297});
//			ret = EL001_util.cleanValue(change, new String[]{"multiList01"});
//			ret = EL001_util.hasValue(change, new String[]{"multiList01"});
//
//		} catch (APIException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		log.log(ret);
//	}
//
	void initMap()
	{
		model_type_map.put("Data Sheet Release Form ( Data Sheet 文件申請單 )", 1540);
		model_type_map.put("Design Changes", 0);
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
		model_type_map.put("Development Documents Request", 0);
		model_type_map.put("DFCO", 0);
		model_type_map.put("Document Obsolete (  文件作廢申請單 )", 0);
		model_type_map.put("Drawing Document Release ( 圖面文件申請單 )", 1564);
		model_type_map.put("ECN", 1564);
		model_type_map.put("ECO", 0);
		model_type_map.put("ERP Data Migration", 0);
		model_type_map.put("Internal Production Release", 1543);
		model_type_map.put("Manuf Spec Release ( 製規申請單 )", 1564);
		model_type_map.put("New Parts Request", 0);
		model_type_map.put("Parts Approval", 0);
		model_type_map.put("Product Document Release ( 產品文件申請單 )", 1564);
		model_type_map.put("Production Release", 1543);
		model_type_map.put("Technical Re-Engineering Documents Release", 0);
		model_type_map.put("Appeal Request", 0);
		model_type_map.put("Customer Request Form (客戶產品RoHS意見處理回覆表)", 0);
		model_type_map.put("Document Copy Request ( 文件調閱申請單 )", 0);
		model_type_map.put("ECR",1564);
	}


	/*
	 * EL001_1
	 * 全部表單複製時清空model type(cover page的change type?)
	 */
	@Override
	public EventActionResult doAction(IAgileSession arg0, INode arg1, IEventInfo req) {
		// TODO Auto-generated method stub
		//IAgileSession session = EL001_util.connect();
		ISaveAsEventInfo info = (ISaveAsEventInfo) req;
		msg.delete(0, msg.length());
		initMap();

		boolean ret = false, result = true;
		try {
			log.log("開始執行EL001_12");

			IChange change = (IChange) arg0.getObject(IChange.OBJECT_TYPE, info.getNewNumber());

			log.log("清空Model Type");
			if(model_type_map.get(change.getAgileClass().getName())!=0)
			{
				ret = (EL001_util.cleanValue(change, new int[]{model_type_map.get(change.getAgileClass().getName())}));
			}else if(model_type_map.get(change.getAgileClass().getName())==0){
				log.log("無Model Type");
			}
			else{
				ret = false;
			}
			log.log(1,ret?"成功":"失敗");
			msg.append("清空Model Type: "+(ret?"成功":"失敗"));
			result = ret;

			ArrayList<String> strList = new ArrayList<String>();
			for(String s : str)
				strList.add(s);

			if(strList.contains(change.getAgileClass().getName()))
			{
				String[] str = new String[]{"Waiting"};
				int baseID[] = new int[]{PDF_Convert_Status};
				log.log("設定PDF Convert Status為Waiting");
				ret = EL001_util.setValues(change, baseID, str);
				log.log(1,ret?"成功":"失敗");
				msg.append("設定PDF Convert Status為Waiting: "+(ret?"成功":"失敗"));
				result = ret;
			}
			if(change.getAgileClass().getName().toLowerCase().contains("developing request"))
			{
				ret = EL001_util.cleanValue(change, new int[]{Require_date, Get_date});
				msg.append("清空欄位: "+(ret?"有值":"無值"));
				log.log("清空欄位: "+(ret?"有值":"無值"));
				String[] str = new String[]{"Waiting"};
				int baseID[] = new int[]{PDF_Convert_Status};
				log.log("設定PDF Convert Status為Waiting");
				ret = EL001_util.setValues(change, baseID, str);
				log.log(1,ret?"成功":"失敗");
				msg.append("設定PDF Convert Status為Waiting: "+(ret?"成功":"失敗"));
				result = ret;
			}
			log.log("end");
			if(result == false)
				throw new Exception();

			return new EventActionResult(req, new ActionResult(ActionResult.STRING, "EL0012程式結束"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			StringBuilder sb = new StringBuilder();
			sb.append(e.toString()).append("\r\n");
			for (StackTraceElement elem : e.getStackTrace()) {
				sb.append("\tat ").append(elem).append("\r\n");
			}
			log.log(sb.toString()+"\r\n");
			return new EventActionResult(req, new ActionResult(ActionResult.EXCEPTION, new Exception(msg.toString())));
		}


	}

}
