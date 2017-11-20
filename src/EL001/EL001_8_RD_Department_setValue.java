package EL001;

import com.agile.api.ChangeConstants;
import com.agile.api.IAgileSession;
import com.agile.api.IChange;
import com.agile.api.IDataObject;
import com.agile.api.INode;
import com.agile.api.IUser;
import com.agile.px.ActionResult;
import com.agile.px.ICustomAction;

public class EL001_8_RD_Department_setValue  implements ICustomAction{
	static IAgileSession adminsession = null;

	public ActionResult doAction(IAgileSession session, INode actionNode, IDataObject affectedObject) {
		try {
			IChange change = (IChange) affectedObject;
			adminsession = EL001_util.connect();
			IChange changeAdmin = (IChange) adminsession.getObject(ChangeConstants.CLASS_CHANGE_BASE_CLASS,
					change.getName());
			if(change.getAgileClass().getName().contains("Production Release")){
				IUser rd = (IUser) change.getCell(1539).getReferent();//取得RD人員
				String Department = null;
				try {
					Department = rd.getValue(2021).toString();//取得RD人員部門資訊
				} catch (NullPointerException e) {
				}
				changeAdmin.setValue(1546, Department); // The Department In Charge ( 執行單位部門 )
			}
			return new ActionResult(ActionResult.STRING, "程式執行結束");
		} catch (Exception e) {
			return new ActionResult(ActionResult.EXCEPTION, e);
		}
	}

}
