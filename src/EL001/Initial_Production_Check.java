package EL001;

import java.util.Iterator;

import com.agile.api.APIException;
import com.agile.api.ChangeConstants;
import com.agile.api.IAgileSession;
import com.agile.api.IChange;
import com.agile.api.IDataObject;
import com.agile.api.IItem;
import com.agile.api.INode;
import com.agile.api.IRow;
import com.agile.api.ITable;
import com.agile.px.ActionResult;
import com.agile.px.ICustomAction;

public class Initial_Production_Check  implements ICustomAction {
	static String sb = "";
	static IAgileSession adminsession = null;
	public ActionResult doAction(IAgileSession session, INode actionNode,IDataObject affectedObject) {
		try {
			IChange change = (IChange) affectedObject;
			adminsession = EL001_util.connect();
			IChange changeAdmin = (IChange) adminsession.getObject(ChangeConstants.CLASS_CHANGE_BASE_CLASS,
					change.getName());
			ITable changeTab = change.getTable(ChangeConstants.TABLE_RELATIONSHIPS);
			Iterator<?> it_changeTab = changeTab.iterator();
			boolean match = false;
			while(it_changeTab.hasNext()){
				IRow row = (IRow) it_changeTab.next();
				if(row.getReferent().getAgileClass().getName().contains("D01-Manufacturing specifications製造規格")){
					sb=row.getReferent().getAgileClass().getName();
					match=true;
				}
			}
			if(match){
				sb+=("Manufacturing Document have been generated");
				changeAdmin.setValue(1589, "Manufacturing Document have been generated");
			}else{
				changeAdmin.setValue(1589, null);
			}
			if(sb.isEmpty()){
				sb="";
			}
			changeTab = change.getTable(ChangeConstants.TABLE_AFFECTEDITEMS);
			it_changeTab = changeTab.iterator();
			match = false;
			while(it_changeTab.hasNext()){
				IRow row = (IRow) it_changeTab.next();
				IItem it = (IItem) row.getReferent();
				if(it.getAgileClass().getName().toLowerCase().contains("semi")){
					sb=it.getAgileClass().getName();
					match=true;
				}
			}
			if(match){
				sb+=("Semi-Finished Products Release");
				changeAdmin.setValue(1589, "Semi-Finished Products Release");
			}
			return new ActionResult(ActionResult.STRING, sb);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			return new ActionResult(ActionResult.EXCEPTION, e);
		}

	}

}
