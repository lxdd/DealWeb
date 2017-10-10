package cn.com.eju.deal.base.helper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.eju.deal.base.dto.log.ErrorLogDto;
import cn.com.eju.deal.base.log.service.ErrorLogService;
import cn.com.eju.deal.core.support.Constant;
import cn.com.eju.deal.core.support.SystemCfg;
import cn.com.eju.deal.core.util.StringUtil;

/**   
* 写日志的共通
* @author (li_xiaodong)
* @date 2015年10月23日 下午1:40:33
*/
public class LogHelper
{
    
    /**
     * 以包名为key的所有日志记录对象
     */
    public static Map<String, LogHelper> logHelper = new Hashtable<String, LogHelper>();
    
    private Logger logger = null;
    
    /**
     * 根据包名实例化log4j对象
     * @param moduleName
     */
    private LogHelper(String moduleName)
    {
        logger = LoggerFactory.getLogger(moduleName);
    }
    
    /**
     * 根据类名获取日志对象
     * @param clazz 类型
     * @return 日志对象
     */
    public static LogHelper getLogger(Class<?> clazz)
    {
        //cn.com.eju.deal
        String packageName = StringUtil.getPrePackageOfPackageName(clazz.getPackage().getName());
        if (logHelper.containsKey(packageName))
        {
            return logHelper.get(packageName);
        }
        else
        {
            return createLogger(packageName);
        }
    }
    
    /**.
     * 根据原类级日志对象与日志等级来构造新的日志对象
     * 
     * @param level 日志等级(error,worn,debug,info)
     * @return 日志对象
     */
    private LogHelper getLoggerByLevel(String level)
    {
        //com.eju.deal.xxx.per\warn...
        String packageName = logger.getName();
        //防止缓存机制
        if (StringUtil.isNotEmpty(logger.getName()) && logger.getName().endsWith(level))
        {
            //判断是否已包含其它级别
            if (isLevelLogger(packageName) && -1 != packageName.indexOf(Constant.LOG4J_JOIN_SIGN))
            {
                //去掉其它级别，拼装当前级别
                packageName =
                    StringUtil.joinStr(packageName.substring(0, packageName.lastIndexOf(Constant.LOG4J_JOIN_SIGN)),
                        Constant.LOG4J_JOIN_SIGN,
                        level);
            }
            else
            {
                packageName = StringUtil.joinStr(packageName, Constant.LOG4J_JOIN_SIGN, level);
            }
        }
        else
        {
            packageName = StringUtil.joinStr(packageName, Constant.LOG4J_JOIN_SIGN, level);
            ;
        }
        //避免同步锁，以便快速获取日志对象
        if (logHelper.containsKey(packageName))
        {
            return logHelper.get(packageName);
        }
        else
        {
            return createLogger(packageName);
        }
    }
    
    /**.
     * 校验类路径是否已包含级别信息
     * 
     * @param packName 类路径
     * @return true : 包含, false : 不包含
     */
    private boolean isLevelLogger(String packName)
    {
        if (packName.endsWith(Constant.LOG4J_LEVEL_INFO) || packName.endsWith(Constant.LOG4J_LEVEL_DEBUG)
            || packName.endsWith(Constant.LOG4J_LEVEL_ERROR) || packName.endsWith(Constant.LOG4J_LEVEL_WARN))
        {
            return true;
        }
        return false;
    }
    
    /**.
     * 根据类径获取日志对象
     * 
     * @param   packageName 类路径
     * @return  日志对象
     * 
     * 由原来的getLogger方法中抽取目的用于区分日志（类路径\Log级别）
     */
    private synchronized static LogHelper createLogger(String packageName)
    {
        LogHelper logService = new LogHelper(packageName);
        logHelper.put(packageName, logService);
        return logService;
    }
    
    /**
     *
     * <p>记录性能日志。</p>
     *
     * @param funcDesc 功能描述
     * @param flag 起始标识：0、开始；1、结束。
     */
    public void performance(String funcDesc, String flag)
    {
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        StringBuffer sb = new StringBuffer();
        sb.append(formateTime(pattern, Calendar.getInstance().getTime()));
        sb.append(" 功能：");
        sb.append(funcDesc);
        sb.append(" 起始标识：");
        if ("0".equals(flag))
        {
            sb.append("开始");
        }
        else
        {
            sb.append("结束");
        }
        getLoggerByLevel(Constant.LOG4J_LEVEL_INFO).logger.info(sb.toString());
    }
    
    /**
     * 记录异常日志
     * @param code 异常代码
     * @param description 异常描述
     * @param e 抛出异常内容
     * @param object 异常操作对象
     */
    public void error(String code, String description, Throwable e, Object object)
    {
        
        StringBuffer sb = new StringBuffer();
        sb.append(" 异常编号：[");
        sb.append(code);
        sb.append("] - ");
        sb.append(description);
        sb.append(" parameters [ ");
        sb.append(object);
        sb.append(" ]\n");
        if (null != e)
        {
            sb.append(e.toString());
            sb.append(Arrays.toString(e.getStackTrace()));
        }
        logger.error(sb.toString());
    }
    
    /**
     * 记录异常日志
     * @param description 异常描述
     * @param e 抛出异常内容
     * @param object 异常操作对象
     */
    public void error(String description, Throwable e)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(description);
        sb.append(" ]\n");
        if (null != e)
        {
            sb.append(e.toString());
            sb.append(Arrays.toString(e.getStackTrace()));
        }
        
        logger.error(sb.toString());
    }
    
    /**
     * 记录异常日志
     * @param description 异常描述
     *
     */
    public void error(String description)
    {
        logger.error(description);
    }
    
    /**
     * 记录异常日志
     * @param description 异常描述
     * @param e 抛出异常内容
     * @param object 异常操作对象
     */
    public void error(String moduleName, String className, String methodName, String parameter, Integer operateId,
        String ipAddress, String description, Throwable e)
    {
        //系统名
        String systemName = SystemCfg.getString("systemName");
        
        StringBuffer sb = new StringBuffer();
        sb.append("ErrorInfo：[");
        sb.append(" operateId：");
        sb.append(operateId);
        sb.append(",");
        sb.append(" systemName：");
        sb.append(systemName);
        sb.append(",");
        sb.append(" moduleName：");
        sb.append(moduleName);
        sb.append(",");
        sb.append(" className：");
        sb.append(className);
        sb.append(",");
        sb.append(" methodName：");
        sb.append(methodName);
        sb.append("] - ");
        sb.append(description);
        sb.append(" parameters [ ");
        sb.append(parameter);
        sb.append(" ]\n");
        if (null != e)
        {
            sb.append(e.toString());
            sb.append(Arrays.toString(e.getStackTrace()));
        }
        logger.error(sb.toString());
        
        //截取e
        String exceptionInfo = null;
        if (null != e)
        {
            StringBuffer errorSb = new StringBuffer();
            errorSb.append(e.toString());
            errorSb.append(Arrays.toString(e.getStackTrace()));
            
            int errorSbLength = errorSb.toString().length();
            if(errorSbLength > 3000){
                errorSbLength = 3000;
            }
            
            exceptionInfo = errorSb.toString().substring(0, errorSbLength);
        }
        
        //调用入LOG_Error表
        ErrorLogService errorLogService = new ErrorLogService();
        
        ErrorLogDto errorLogDto = new ErrorLogDto();
        errorLogDto.setSystemName(systemName);
        errorLogDto.setModuleName(moduleName);
        errorLogDto.setClassName(className);
        errorLogDto.setMethodName(methodName);
        errorLogDto.setParameter(parameter);
     
        errorLogDto.setIpAddress(ipAddress);
        errorLogDto.setExceptionInfo(exceptionInfo);
        errorLogDto.setDescription(description);
        
        errorLogDto.setUserIdCreate(operateId);
        
        
        try
        {
            errorLogService.create(errorLogDto);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
            logger.error(systemName + " logHelper create errorLog failure");
        }
        
    }
    
    /**
     * 记录警告日志
     * @param code 警告代码
     * @param description 异常描述
     * @param e 抛出异常内容
     * @param object 异常操作对象
     */
    public void warn(String code, String description, Throwable e, Object object)
    {
        
        StringBuffer sb = new StringBuffer();
        sb.append(" 警告编号：[");
        sb.append(code);
        sb.append("] - ");
        sb.append(description);
        sb.append(" parameters [ ");
        sb.append(object);
        sb.append(" ]\n");
        if (null != e)
        {
            sb.append(e.toString());
            sb.append(Arrays.toString(e.getStackTrace()));
        }
        
        logger.warn(sb.toString());
    }
    
    /**
     * 记录警告日志
     * @param description 异常描述
     */
    public void warn(String description)
    {
        logger.warn(description);
    }
    
    /**
     * 记录信息日志
     * @param description 信息描述
     * @param object 信息操作对象
     */
    public void info(String description, Object object)
    {
        
        StringBuffer sb = new StringBuffer();
        sb.append(description);
        sb.append(" parameters [ ");
        sb.append(object);
        sb.append(" ]\n");
        logger.info(sb.toString());
    }
    
    /**
     * 记录信息日志
     * @param description 信息描述
     */
    public void info(String description)
    {
        logger.info(description);
    }
    
    /**
     *
     * <p>请记述函数的功能概要。</p>
     *
     * @param pattern
     * @param date
     * @return
     */
    private String formateTime(String pattern, Date date)
    {
        return new SimpleDateFormat(pattern).format(date);
    }
    
    public void debug(Object message)
    {
        logger.debug(message == null ? "null" : message.toString());
    }
    
    public void debug(Object message, Throwable t)
    {
        logger.debug(message == null ? "null" : message.toString(), t);
    }
    
    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }
    
    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }
    
    public boolean isWarnEnabled()
    {
        return logger.isWarnEnabled();
    }
    
    public boolean isErrorEnabled()
    {
        return logger.isErrorEnabled();
    }
}
