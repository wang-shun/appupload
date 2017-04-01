package com.galaxy.appupload.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.galaxy.appupload.beans.ApplicationInfoBean;
import com.galaxy.appupload.beans.R_versionInfoBean;
import com.galaxy.appupload.beans.VersionInfoBean;
import com.galaxy.appupload.controller.AppManagerController;
import com.galaxy.appupload.dao.AppManagerDao;
import com.galaxy.appupload.util.QRCodeUtil;
import com.galaxy.appupload.util.RDataToJson;
import com.galaxy.appupload.util.ReadProperties;
import com.galaxy.appupload.util.Static_Commond;
import com.galaxy.appupload.util.UUIDGenerator;
import com.google.gson.Gson;
import com.mysql.jdbc.StringUtils;

@Service(value="appManagerService")
public class AppManagerServiceImpl implements AppManagerService{
	private static final Logger log = LoggerFactory.getLogger(AppManagerController.class);
	//ת����
	Gson gson = new Gson();
	private RDataToJson  Ifinte= new RDataToJson();
	@Autowired
	AppManagerDao appManagerDao;
	
	/**
	 * ����Ӧ�÷���
	 */
	@Override
	public int saveApplication(ApplicationInfoBean applicationInfoBean, HttpServletRequest request,MultipartFile smallLogo, MultipartFile bigLogo) {
		String smallPath = "";
		String bigPath = "";
		//��ȡҳ����Ϣ
		String appcode = request.getParameter("appcode");
		String systemType = request.getParameter("systemType");
		String type="";
		if("0".equals(systemType)){
			type="Ios";
		}else{
			type="Android";
		}
		applicationInfoBean.setSystemType(type);
		applicationInfoBean.setAppcode(appcode);
		//Сͼ���ȡ
		if (!smallLogo.isEmpty()) {
			try {
				String[] s1 = smallLogo.getOriginalFilename().split("\\.");
				String nums = getRandomString(6);
				// ���ݿⱣ����ļ�����
				String attachType = s1[1];
				String path = request.getSession().getServletContext().getRealPath("/")+"\\file"+"\\"+nums+"\\";
				String basePath =  path + s1[0] + "." + attachType;
				File files = new File(basePath);
				if(!files.exists() && !files.mkdirs()){
					files.mkdirs();
				}
				smallLogo.transferTo(new File(basePath));
				smallPath = "file" + basePath.split("file")[1];
				applicationInfoBean.setLogoSmallFile(smallPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//��ͼ���ȡ
		if (!bigLogo.isEmpty()) {
	        try {
	        	String[] s1 = bigLogo.getOriginalFilename().split("\\.");
	        	String nums = getRandomString(6);
				// ���ݿⱣ����ļ�����
				String attachType = s1[1];
				String path = request.getSession().getServletContext().getRealPath("/")+"\\file"+"\\"+nums+"\\";
				String basePath =  path + s1[0] + "." + attachType;
				File files = new File(basePath);
				if(!files.exists() && !files.mkdirs()){
					files.mkdirs();
				}
				bigLogo.transferTo(new File(basePath));
				bigPath = "file" + basePath.split("file")[1];
				applicationInfoBean.setLogoBigFile(bigPath);
			} catch (IOException e) {
				e.printStackTrace();
			} 

		}
		//����32λID
		String ID = UUIDGenerator.getUUID();
		applicationInfoBean.setId(ID);
		//dao
		int result=0;
		ApplicationInfoBean application = appManagerDao.getAppBean(appcode,type);
		if(application!=null){
			if(!(StringUtils.isNullOrEmpty(bigPath)) && !(StringUtils.isNullOrEmpty(application.getLogoBigFile()))){
				File fl =new File(request.getSession().getServletContext().getRealPath("/")+"\\"+application.getLogoBigFile());
				fl.delete();
				fl.getParentFile().delete();
			}
			if(!(StringUtils.isNullOrEmpty(smallPath)) && !(StringUtils.isNullOrEmpty(application.getLogoSmallFile()))){
				File f2 =new File(request.getSession().getServletContext().getRealPath("/")+"\\"+application.getLogoSmallFile());
				f2.delete();
				f2.getParentFile().delete();
			}
			if(!(StringUtils.isNullOrEmpty(bigPath)&&StringUtils.isNullOrEmpty(smallPath))){
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("small", smallPath);
				params.put("big", bigPath);
				params.put("id", application.getId());
				result = appManagerDao.updateApplication(params);
			}
			result=1;
		}else{
			result = appManagerDao.saveApplication(applicationInfoBean);
		}
		return result;
	}
	
	/**
	 * ����汾��Ϣ����
	 * @throws Exception 
	 */
	@Override
	public int saveVersion(VersionInfoBean versionInfoBean, HttpServletRequest request, MultipartFile file) throws Exception {
		String filePath = "";
		String qrpath ="";
		String path ="";
		int result =0;
		String fileName="";
		int vstatus=0;
		String qr="";
		
		//��ȡ6λ�����
		String nums = getRandomString(6);
		//��ҳ���ȡֵ
		String appcode = request.getParameter("appName");
		String apptype = request.getParameter("appType");
		String version = request.getParameter("version");
		String describe = request.getParameter("describe");
		String status = request.getParameter("status");
		
		if("0".equals(status)){
			vstatus=0;
		}else{
			vstatus=1;
		}
		
		//��ȡappid
		String appid = appManagerDao.getAppId(appcode,apptype);
		//����32λID
		String ID = UUIDGenerator.getUUID();
		//ϵͳ��ǰʱ��
		SimpleDateFormat ff =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date= new Date();
		
		//app�ļ���ȡ
		if (!file.isEmpty()) {
			try {
				String[] s1 = file.getOriginalFilename().split("\\.");
				// ���ݿⱣ����ļ�����
				String attachType = s1[1];
				
				path = request.getSession().getServletContext().getRealPath("/")+"file"+"\\"+nums+"\\";
				String basePath =  path + s1[0] + "." + attachType;
				
				qrpath = request.getSession().getServletContext().getRealPath("/");
				File files = new File(basePath);
				if(!files.exists() && !files.mkdirs()){
					files.mkdirs();
				}
				file.transferTo(new File(basePath));
				filePath = "file" + basePath.split("file")[1];
				fileName=s1[0] + "." + attachType;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//����plist����
		String appupload_url = ReadProperties.getRescMap().get("appupload_url");
		
		if("Ios".equals(apptype)||"ios".equals(apptype)){
			//����plist����
			createplist(path,appupload_url+filePath,version,request);
			//String url = ReadProperties.getRescMap().get("url");
			qr=appupload_url+"download/app.action?nums="+nums+"&appupload_url="+appupload_url;
		}else{
			qr =appupload_url+filePath;
		}
		//���ɶ�ά��
		//String qr ="http://192.168.99.212:8080/appupload/appManager/qrCodeDownload.action?filePath="+filePath;
		String qrcode = QRCodeUtil.encode(qr, "",qrpath, true);
		
		
		//dao�ж�
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", appid);
		params.put("version", version);
		params.put("vstatus", vstatus);
		VersionInfoBean info = appManagerDao.isExistVersion(params);
		if(info!=null){
			File fl =new File(request.getSession().getServletContext().getRealPath("/")+"\\"+info.getQrCode());
			if(fl.isFile() && fl.exists()){
				fl.delete();
			}
			if(!(StringUtils.isNullOrEmpty(filePath)) && !(StringUtils.isNullOrEmpty(info.getFilepath()))){
				File f2 =new File(request.getSession().getServletContext().getRealPath("/")+"\\"+info.getFilepath());
				File f3 =new File(f2.getParentFile().toString()+"\\"+"stars.plist");
				if(f2.isFile() && f2.exists()){
					f2.delete();
				}
				if(f3.isFile() && f3.exists()){
					f3.delete();
				}
				f2.getParentFile().delete();
			}
			info.setCreateTime(ff.format(date));
			info.setFilename(fileName);
			info.setFilepath(filePath);
			info.setUpdatelog(describe);
			info.setQrCode(qrcode);
			result = appManagerDao.updateVsersion(info);
		}else{
			versionInfoBean.setVersionStatus(vstatus);
			versionInfoBean.setAppid(appid);
			versionInfoBean.setVersionNo(version);
			versionInfoBean.setUpdatelog(describe);
			versionInfoBean.setId(ID);
			versionInfoBean.setCreateTime(ff.format(date));
			versionInfoBean.setFilename(fileName);
			versionInfoBean.setFilepath(filePath);
			versionInfoBean.setQrCode(qrcode);
			result = appManagerDao.saveVsersion(versionInfoBean);
		}
		return result;
	}
	/**
	 * ��ȡappӦ��list
	 */
	public List<ApplicationInfoBean> getAppList(){
		return appManagerDao.getAppList();
	}
	/**
	 * ��ȡϵͳ����by appname
	 */
	public List<ApplicationInfoBean> getSysType(String appname) {
		return appManagerDao.getSysTypeList(appname);
	}
	/**
	 * ��ȡ�汾�ͺ�list
	 */
	@Override
	public List<VersionInfoBean> getSysVersion(String appname, String apptype,String status) {
		String appid = appManagerDao.getAppId(appname,apptype);
		List<VersionInfoBean> versionInfoBean= appManagerDao.getVersionList(appid,status);
		return versionInfoBean;
	}
	/**
	 * ��ȡ�汾״̬list
	 */
	@Override
	public List<VersionInfoBean> getVerStatus(String appname, String apptype) {
		String appid = appManagerDao.getAppId(appname,apptype);
		List<VersionInfoBean> versionInfoBean= appManagerDao.getStatusList(appid);
		return versionInfoBean;
	}
	/**
	 * ��ά���ȡ�汾������Ϣ����
	 */
	@Override
	public VersionInfoBean getNewVersionByStatus(String id, String status) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", id);
		params.put("appcode", status);
		VersionInfoBean versionInfoBean= appManagerDao.getNewVersionInfo(params);
		return versionInfoBean;
	}
	
	/**
	 * ���ط���
	 */
	@Override
	public VersionInfoBean downloadFile(String code, String type, String status) {
		String appid = appManagerDao.getAppId(code,type);
		int flag=0;
		if("beta".equals(status)){
			flag=0;
		}else if("release".equals(status)){
			flag=1;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", appid);
		params.put("appcode", flag);
		VersionInfoBean versionInfoBean= appManagerDao.getNewVersionInfo(params);
		return versionInfoBean;
	}
	/**
	 * ��ȡ���°汾��Ϣ
	 */
	@Override
	public String getVersionInfo(String clientName, String systemType, String appCode) {
		String resp="";
		int flag=0;
		String dataValue="";
		
		R_versionInfoBean r_versionInfo = new R_versionInfoBean();
		String appupload_url = ReadProperties.getRescMap().get("appupload_url");
		
		//��ȡappid
		String appid = appManagerDao.getAppId(clientName,systemType);
		if(!StringUtils.isNullOrEmpty(appid)){
			if("beta".equals(appCode)){
				flag=0;
			}else if("release".equals(appCode)){
				flag=1;
			}
			//ִ��dao
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("appid", appid);
			params.put("appcode", flag);
			VersionInfoBean versionInfoBean = appManagerDao.getNewVersionInfo(params);
			if(versionInfoBean!=null){
				if("Ios".equals(systemType)||"ios".equals(systemType)){
					//String httpurl = ReadProperties.getRescMap().get("url");
					String[] ss = versionInfoBean.getFilepath().split("\\\\");
					String url =appupload_url+"download/app.action?nums="+ss[1]+"&appupload_url="+appupload_url;
					r_versionInfo.setUrl(url);
				}else{
					r_versionInfo.setUrl(ReadProperties.getRescMap().get("appupload_url")+versionInfoBean.getFilepath());
				}
				r_versionInfo.setClientVersion(versionInfoBean.getVersionNo());
				r_versionInfo.setUpdateLog(versionInfoBean.getUpdatelog());
				//gsonתjson
				dataValue = gson.toJson(r_versionInfo);
				// ��ȡ��Ϣjson�ַ���
				resp = Ifinte.getDataJson(Static_Commond.SUCCESS,  ReadProperties.getRescMap().get("success"),dataValue);
				log.info("���ؽ��:"+resp);
				return resp;
			}else{
				// ��ȡ��Ϣjson�ַ���
				resp = Ifinte.getDataJson(Static_Commond.RESULTNULL,  ReadProperties.getRescMap().get("result_NULL"),"");
				log.info("δ�鵽��ذ汾��Ϣ�����ؽ��:"+resp);
				return resp;
			}
		}else{
			// ��ȡ��Ϣjson�ַ���
			resp = Ifinte.getDataJson(Static_Commond.RESULTNULL,  ReadProperties.getRescMap().get("result_NULL"),"");
			log.info("δ�鵽��ذ汾��Ϣ�����ؽ��:"+resp);
			return resp;
		}
	}
	
	//�������nλ����
	public String getRandomString(int n){
		char[] str1 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		int i;
		int count = 0;
		StringBuffer authcode = new StringBuffer("");
		Random r = new Random();
		while (count < n) {
			i = Math.abs(r.nextInt(10));
			if (i >= 0 && i < str1.length) {
				authcode.append(str1[i]);
				count++;
			}
		}
		return authcode.toString();
	}
	
	//����plist�ļ�����
	public void createplist(String iosfile,String ipa, String version, HttpServletRequest request){
		log.info("����plist�ļ���ʼ");
		String path = iosfile+"\\"+"stars.plist";
		FileOutputStream fos = null;
		try {
			// �����ĵ�����
			DocType docType = new DocType("plist");
			docType.setPublicID("-//Apple//DTD PLIST 1.0//EN");
			docType.setSystemID("http://www.apple.com/DTDs/PropertyList-1.0.dtd");
			// �������ڵ� plist
			Element root = new Element("plist");
			root.setAttribute("version", "1.0");
			//
			Element rootDict = new Element("dict");
			rootDict.addContent(new Element("key").setText("items"));
			Element rootDictArray = new Element("array");
			Element rootDictArrayDict = new Element("dict");
			rootDictArrayDict.addContent(new Element("key").setText("assets"));
	
	
			Element rootDictArrayDictArray = new Element("array");
			Element rootDictArrayDictArrayDict1 = new Element("dict");
			rootDictArrayDictArrayDict1.addContent(new Element("key")
			.setText("kind"));
			rootDictArrayDictArrayDict1.addContent(new Element("string")
			.setText("software-package"));
			rootDictArrayDictArrayDict1.addContent(new Element("key")
			.setText("url"));
			rootDictArrayDictArrayDict1.addContent(new Element("string")
			.setText(ipa.replace("\\", "/")));
	
			rootDictArrayDictArray.addContent(rootDictArrayDictArrayDict1);
			rootDictArrayDict.addContent(rootDictArrayDictArray);
			rootDictArrayDict.addContent(new Element("key").setText("metadata"));
	
			Element rootDictArrayDictDict = new Element("dict");
			rootDictArrayDictDict.addContent(new Element("key")
			.setText("bundle-identifier"));
			rootDictArrayDictDict.addContent(new Element("string")
			.setText("com.galaxyinternet.galaxystars"));
			rootDictArrayDictDict.addContent(new Element("key")
			.setText("bundle-version"));
			rootDictArrayDictDict.addContent(new Element("string").setText(version));
			rootDictArrayDictDict.addContent(new Element("key").setText("kind"));
			rootDictArrayDictDict.addContent(new Element("string")
			.setText("software"));
			rootDictArrayDictDict.addContent(new Element("key").setText("title"));
			rootDictArrayDictDict.addContent(new Element("string").setText("����"));
			rootDictArrayDict.addContent(rootDictArrayDictDict);
	
			rootDictArray.addContent(rootDictArrayDict);
			rootDict.addContent(rootDictArray);
			root.addContent(rootDict);
			// ���ڵ����ӵ��ĵ���;
			Document Doc = new Document(root, docType);
			Format format = Format.getPrettyFormat();
			XMLOutputter XMLOut = new XMLOutputter(format);
			// ��� user.xml �ļ���
			fos = new FileOutputStream(new File(path));
			XMLOut.output(Doc, fos);
		} catch (IOException e) {
			e.printStackTrace();
			log.info("����plist�ļ��쳣");
		}finally{
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
					log.info("����plist�ļ��쳣");
				}
			}
		}
		log.info("����plist�ļ��ɹ�");
	}
	 
}