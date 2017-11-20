package EL001;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.DataTypeConstants;
import com.agile.api.IAgileList;
import com.agile.api.IAgileSession;
import com.agile.api.ICell;
import com.agile.api.IChange;
import com.agile.api.IUser;

public class EL001_util {
	static String CONFIG = "C:\\Agile\\Agile935\\integration\\sdk\\extensions\\SVConfig.txt";
//	public static void main(String[] args)
//	{
////		IAgileSession session = connect();
//		
//		try 
//		{
//			IChange change = (IChange) session.getObject(IChange.OBJECT_TYPE, "EL3_018");
//			
//			int[] baseID = new int[]{1575,1564,1060};
//			
//			System.out.println(cleanValue(change,baseID));
//		}
//		catch (APIException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	static IAgileSession connect() {
		IAgileSession session = null;
		try {
			FileReader fr = new FileReader(CONFIG);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			String url ="";
			String user ="";
			String pwd ="";
			while ((line = br.readLine()) != null) {
				if (line.contains("url")) {
					url = line.substring(line.indexOf("=") + 1, line.length()); // 取得URL
				} else if (line.contains("user")) {
					user = line.substring(line.indexOf("=") + 1, line.length()); // 取得user
				} else if (line.contains("pwd")) {
					pwd = line.substring(line.indexOf("=") + 1, line.length()); // 取得pwd
				}
			}
			fr.close();
			System.setProperty("disable.agile.sessionID.generation", "true");
			HashMap params = new HashMap();
			params.put(AgileSessionFactory.USERNAME, user);
			params.put(AgileSessionFactory.PASSWORD, pwd);
			AgileSessionFactory factory;
			factory = AgileSessionFactory.getInstance(url);
			session = factory.createSession(params);
			//log.log("成功登入Agile");
		} catch (APIException e) {
			//log.log(e);
			//log.log("連接Agile時發生錯誤，請檢查config是否輸入錯誤");
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

		return session;
	}
	/*
	 * hasValue
	 * 傳入baseID檢查是否全部有值
	 */
	static boolean hasValue(IChange change, int baseID[])
	{
		try {
			for(int i = 0; i < baseID.length; i++)
			{
				ICell c = (ICell) change.getCell(baseID[i]);

				switch(c.getAttribute().getDataType())
				{
					case DataTypeConstants.TYPE_STRING:
						if(c.getValue().toString().isEmpty())
							return false;
						break;
					case DataTypeConstants.TYPE_SINGLELIST:
						IAgileList[] selected = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected.length == 0)
						{
							return false;
						}
						break;
					case DataTypeConstants.TYPE_MULTILIST:
						IAgileList[] selected1 = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected1.length == 0)
						{
							return false;
						}
						break;
					case DataTypeConstants.TYPE_DATE:
						if(c.getValue() == null)
							return false;
						break;
					default:
						return false;
				}
				//System.out.println(c.getName());
			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	static boolean hasValue(IChange change, String baseID[])
	{
		try {
			for(int i = 0; i < baseID.length; i++)
			{
				ICell c = (ICell) change.getCell(baseID[i]);

				switch(c.getAttribute().getDataType())
				{
					case DataTypeConstants.TYPE_STRING:
						if(c.getValue().toString().isEmpty())
							return false;
						break;
					case DataTypeConstants.TYPE_SINGLELIST:
						IAgileList[] selected = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected.length == 0)
						{
							return false;
						}
						break;
					case DataTypeConstants.TYPE_MULTILIST:
						IAgileList[] selected1 = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected1.length == 0)
						{
							return false;
						}
						break;
					case DataTypeConstants.TYPE_DATE:
						if(c.getValue() == null)
							return false;
						break;
					default:
						return false;
				}
				//System.out.println(c.getName());

			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	static boolean isValue(IChange change, int baseID[], String str[])
	{
		if(baseID.length!=str.length)
			return false;
		try {
			for(int i = 0; i < baseID.length; i++)
			{
				ICell c = (ICell) change.getCell(baseID[i]);

				switch(c.getAttribute().getDataType())
				{
					case DataTypeConstants.TYPE_STRING:
						if(!c.getValue().toString().equals(str))
							return false;
					case DataTypeConstants.TYPE_SINGLELIST:

					case DataTypeConstants.TYPE_MULTILIST:
						IAgileList[] selected = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected.length == 0)
						{
							return false;
						}
						else if(!selected[0].getValue().toString().equals(str[i]))
						{
							return false;
						}

						break;
					case DataTypeConstants.TYPE_DATE:
						Date date = new Date();
						SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH mm ss z yyyy");
						try {


							date = format.parse(str[i]);


						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(!c.getValue().equals(date))
							return false;
						break;
					default:
						return false;
				}
				//System.out.println(c.getName());

			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();return false;
		}
		return true;
	}

	static boolean cleanValue(IChange change, int baseID[])
	{
		try {
			for(int i = 0; i < baseID.length; i++)
			{
				ICell c = (ICell) change.getCell(baseID[i]);

				switch(c.getAttribute().getDataType())
				{
					case DataTypeConstants.TYPE_STRING:

						if(!c.getValue().toString().isEmpty() && !c.isReadOnly())
						{
							System.out.println("string: "+c.getName());
							c.setValue(null);
						}
						break;
					case DataTypeConstants.TYPE_SINGLELIST:

					case DataTypeConstants.TYPE_MULTILIST:
						IAgileList[] selected1 = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected1.length != 0 && !c.isReadOnly())
						{
							System.out.println("list: "+c.getName());
							IAgileList list = (IAgileList) c.getValue();
							list.setSelection(new Object[]{});
							change.setValue(baseID[i], list);
						}
						break;
					case DataTypeConstants.TYPE_DATE:

						if(c.getValue() != null && !c.isReadOnly())
						{
							System.out.println("date: "+c.getName());

							c.setValue(null);
						}
						break;
					default:
						return false;
				}
				//System.out.println(c.getName());
			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}

	static boolean cleanValue(IChange change, String baseID[])
	{
		try {
			for(int i = 0; i < baseID.length; i++)
			{
				ICell c = (ICell) change.getCell(baseID[i]);

				switch(c.getAttribute().getDataType())
				{
					case DataTypeConstants.TYPE_STRING:

						if(!c.getValue().toString().isEmpty() && !c.isReadOnly())
						{
							System.out.println("string: "+c.getName());
							c.setValue(null);
						}
						break;
					case DataTypeConstants.TYPE_SINGLELIST:

					case DataTypeConstants.TYPE_MULTILIST:
						IAgileList[] selected1 = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected1.length != 0 && !c.isReadOnly())
						{
							System.out.println("list: "+c.getName());
							IAgileList list = (IAgileList) c.getValue();
							list.setSelection(new Object[]{});

							change.setValue(c.getId(), list);
						}
						break;
					case DataTypeConstants.TYPE_DATE:

						if(c.getValue() != null && !c.isReadOnly())
						{
							System.out.println("date: "+c.getName());

							c.setValue(null);
						}
						break;
					default:
						return false;
				}
				//System.out.println(c.getName());
			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	static boolean setValues(IChange change, int baseID[], String str[])
	{
		if(baseID.length!=str.length)
			return false;
		try {
			for(int i = 0; i < baseID.length; i++)
			{
				ICell c = (ICell) change.getCell(baseID[i]);

				switch(c.getAttribute().getDataType())
				{
					case DataTypeConstants.TYPE_STRING:

						if(!c.getValue().toString().isEmpty() && !c.isReadOnly())
						{
							System.out.println("string: "+c.getName());
							c.setValue(str[i]);
						}
						break;
					case DataTypeConstants.TYPE_SINGLELIST:

					case DataTypeConstants.TYPE_MULTILIST:
						IAgileList[] selected1 = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected1.length != 0 && !c.isReadOnly())
						{
							System.out.println("list: "+c.getName());
							IAgileList list = (IAgileList) c.getValue();
							list.setSelection(new Object[]{str[i]});
							change.setValue(baseID[i], list);
						}
						break;
					case DataTypeConstants.TYPE_DATE:

						if(c.getValue() != null && !c.isReadOnly())
						{
							System.out.println("date: "+c.getName());

							c.setValue(str[i]);
						}
						break;
					default:
						return false;
				}
				//System.out.println(c.getName());
			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
	public static String getDateTime(){
		SimpleDateFormat sdFormat = new SimpleDateFormat("MMdd");
		sdFormat.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
		Date date = new Date();
		String strDate = sdFormat.format(date);
		return strDate;
	}
	static boolean hasValue(IChange change, int baseID[], String str[])
	{
		if(baseID.length!=str.length)
			return false;
		try {
			for(int i = 0; i < baseID.length; i++)
			{
				ICell c = (ICell) change.getCell(baseID[i]);

				switch(c.getAttribute().getDataType())
				{
					case DataTypeConstants.TYPE_STRING:
						if(!c.getValue().toString().contains(str[i]))
							return false;
					case DataTypeConstants.TYPE_SINGLELIST:

					case DataTypeConstants.TYPE_MULTILIST:
						IAgileList[] selected = (IAgileList[]) ((IAgileList) c.getValue()).getSelection();
						if(selected.length == 0)
						{
							return false;
						}
						else if(!selected[0].getValue().toString().contains(str[i]))
						{
							return false;
						}

						break;
					case DataTypeConstants.TYPE_DATE:
						Date date = new Date();
						SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH mm ss z yyyy");
						try {


							date = format.parse(str[i]);


						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(!c.getValue().equals(date))
							return false;
						break;
					default:
						return false;
				}
				//System.out.println(c.getName());

			}
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();return false;
		}
		return true;
	}
}
