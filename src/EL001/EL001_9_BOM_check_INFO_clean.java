package EL001;

import java.util.Iterator;

import com.agile.api.ChangeConstants;
import com.agile.api.IAgileSession;
import com.agile.api.IChange;
import com.agile.api.IDataObject;
import com.agile.api.INode;
import com.agile.api.IRow;
import com.agile.api.ITable;
import com.agile.api.IUser;
import com.agile.api.StatusConstants;
import com.agile.px.ActionResult;
import com.agile.px.ICustomAction;

public class EL001_9_BOM_check_INFO_clean implements ICustomAction {
	static IAgileSession adminsession = null;

	/**
	 * 針對BOM檢查欄位作清空的動作
	 */
	public ActionResult doAction(IAgileSession session, INode actionNode, IDataObject affectedObject) {
		try {
			IChange change = (IChange) affectedObject;
			adminsession = EL001_util.connect();
			IChange changeAdmin = (IChange) adminsession.getObject(ChangeConstants.CLASS_CHANGE_BASE_CLASS,
					change.getName());
			String status = (String) change.getValue(ChangeConstants.ATT_COVER_PAGE_STATUS).toString().toLowerCase();
			switch (status) {
				//PRD initial
				case "upload reports":
					changeAdmin.setValue(1053, null);
					break;
				//PRD internal
				case "rd mgr review":
					changeAdmin.setValue(1053, null);
					break;
				//ECN
				case "rd dept manager review":
					changeAdmin.setValue(1053, null);
					break;
			}
			return new ActionResult(ActionResult.STRING, "程式執行結束");
		} catch (Exception e) {
			return new ActionResult(ActionResult.EXCEPTION, e);
		}
	}

}
