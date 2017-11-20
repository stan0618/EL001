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
import com.agile.px.ActionResult;
import com.agile.px.ICustomAction;
import com.anselm.plm.utilobj.LogIt;

public class EL001_10_ECN_Status implements ICustomAction {
	static IAgileSession adminsession = null;
	static LogIt log = new LogIt("EL001_10");
	public ActionResult doAction(IAgileSession session, INode actionNode, IDataObject affectedObject) {
		try {
			IUser user = session.getCurrentUser();
			String Department = null;
			try {
				Department = user.getValue(2021).toString(); // 取得部門資訊
				log.log(Department);
			} catch (NullPointerException e) {
			}
			IChange change = (IChange) affectedObject;
			adminsession = EL001_util.connect();
			IChange changeAdmin = (IChange) adminsession.getObject(ChangeConstants.CLASS_CHANGE_BASE_CLASS,
					change.getName());
			if(changeAdmin.getValue(1576).toString().equals("ECR was Released")||changeAdmin.getValue(1576).toString().isEmpty()){
				ITable table = change.getTable(ChangeConstants.TABLE_RELATIONSHIPS);
				Iterator<?> it = table.iterator();
				boolean flag = false;
				// 對relationship掃一遍
				while (it.hasNext()) {
					IRow row = (IRow) it.next();
					// 檢查ECR單是否released
					if (row.getReferent().getAgileClass().getName().equals("ECR")) {
						IChange ecr = (IChange)row.getReferent();
						String status = ecr.getValue(ChangeConstants.ATT_COVER_PAGE_STATUS).toString().toLowerCase();
						log.log(status+"  "+status.equals("release"));
						if (status.equals("release")) {
							flag = true;
						} else {
							flag = false;
							break;
						}
					}
				}
				log.log("ECR STATUS: "+flag);
				// setValue ECR STATUS
				if (flag) {
					changeAdmin.setValue(1576, "ECR was Released");
				}else{
					changeAdmin.setValue(1576, "ECR does not release");
				}
				log.log(changeAdmin.getValue(1576));
				log.log("By Workflow");
			}else if (Department.contains("RD")&&changeAdmin.getValue(1576).toString().equals("ECR does not release")) {
				ITable table = change.getRelationship();
				Iterator<?> it = table.iterator();
				boolean flag = false;
				// 對relationship掃一遍
				while (it.hasNext()) {
					IRow row = (IRow) it.next();
					IChange ecr = (IChange) row.getReferent();
					// 檢查ECR單是否released
					if (ecr.getAgileClass().getName().equals("ECR")) {
						String status = ecr.getValue(ChangeConstants.ATT_COVER_PAGE_STATUS).toString().toLowerCase();
						log.log(status+"  "+status.equals("release"));
						if (status.equals("release")) {
							flag = true;
						} else {
							flag = false;
							break;
						}
					}
				}
				// setValue ECR STATUS
				if (flag) {
					changeAdmin.setValue(1576, "ECR was Released");
				}
				log.log("By Action");
			}
			return new ActionResult(ActionResult.STRING, "程式執行結束");
		} catch (Exception e) {
			return new ActionResult(ActionResult.EXCEPTION, e);
		}
	}
}
