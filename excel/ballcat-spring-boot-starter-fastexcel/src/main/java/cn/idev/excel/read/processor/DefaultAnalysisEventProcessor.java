package cn.idev.excel.read.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.enums.HeadKindEnum;
import cn.idev.excel.enums.RowTypeEnum;
import cn.idev.excel.exception.ExcelAnalysisException;
import cn.idev.excel.exception.ExcelAnalysisStopException;
import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.read.listener.ReadListener;
import cn.idev.excel.read.metadata.holder.ReadRowHolder;
import cn.idev.excel.read.metadata.property.ExcelReadHeadProperty;
import cn.idev.excel.util.ConverterUtils;
import cn.idev.excel.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analysis event
 *
 * @author jipengfei
 */
@SuppressWarnings({ "java:S3776", "java:S3740", "java:S5411", "java:S135", "unchecked" })
public class DefaultAnalysisEventProcessor implements AnalysisEventProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAnalysisEventProcessor.class);

	@Override
	public void extra(AnalysisContext analysisContext) {
		dealExtra(analysisContext);
	}

	@Override
	public void endRow(AnalysisContext analysisContext) {
		if (RowTypeEnum.EMPTY.equals(analysisContext.readRowHolder().getRowType())) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Empty row!");
			}
			if (analysisContext.readWorkbookHolder().getIgnoreEmptyRow()) {
				return;
			}
		}
		dealData(analysisContext);
	}

	@Override
	public void endSheet(AnalysisContext analysisContext) {
		for (ReadListener<?> readListener : analysisContext.currentReadHolder().readListenerList()) {
			readListener.doAfterAllAnalysed(analysisContext);
		}
	}

	private void dealExtra(AnalysisContext analysisContext) {
		for (ReadListener<?> readListener : analysisContext.currentReadHolder().readListenerList()) {
			try {
				readListener.extra(analysisContext.readSheetHolder().getCellExtra(), analysisContext);
			}
			catch (Exception e) {
				onException(analysisContext, e);
				break;
			}
			if (!readListener.hasNext(analysisContext)) {
				throw new ExcelAnalysisStopException();
			}
		}
	}

	private void onException(AnalysisContext analysisContext, Exception e) {
		for (ReadListener<?> readListenerException : analysisContext.currentReadHolder().readListenerList()) {
			try {
				readListenerException.onException(e, analysisContext);
			}
			catch (RuntimeException re) {
				throw re;
			}
			catch (Exception e1) {
				throw new ExcelAnalysisException(e1.getMessage(), e1);
			}
		}
	}

	private void dealData(AnalysisContext analysisContext) {
		ReadRowHolder readRowHolder = analysisContext.readRowHolder();
		Map<Integer, ReadCellData<?>> cellDataMap = (Map) readRowHolder.getCellMap();
		readRowHolder.setCurrentRowAnalysisResult(cellDataMap);
		int rowIndex = readRowHolder.getRowIndex();
		int currentHeadRowNumber = analysisContext.readSheetHolder().getHeadRowNumber();

		boolean isData = rowIndex >= currentHeadRowNumber;

		// Now is data
		for (ReadListener readListener : analysisContext.currentReadHolder().readListenerList()) {
			try {
				if (isData) {
					readListener.invoke(readRowHolder.getCurrentRowAnalysisResult(), analysisContext);
				}
				else {
					readListener.invokeHead(cellDataMap, analysisContext);
				}
			}
			catch (Exception e) {
				onException(analysisContext, e);
				break;
			}
			if (!readListener.hasNext(analysisContext)) {
				throw new ExcelAnalysisStopException();
			}
		}

		// Last head column
		if (!isData && currentHeadRowNumber == rowIndex + 1) {
			buildHead(analysisContext, cellDataMap);
		}
	}

	private void buildHead(AnalysisContext analysisContext, Map<Integer, ReadCellData<?>> cellDataMap) {
		if (!HeadKindEnum.CLASS.equals(analysisContext.currentReadHolder().excelReadHeadProperty().getHeadKind())) {
			return;
		}
		Map<Integer, String> dataMap = ConverterUtils.convertToStringMap(cellDataMap, analysisContext);
		ExcelReadHeadProperty excelHeadPropertyData = analysisContext.readSheetHolder().excelReadHeadProperty();
		Map<Integer, Head> headMapData = excelHeadPropertyData.getHeadMap();
		Map<Integer, Head> tmpHeadMap = new HashMap<>(headMapData.size() * 4 / 3 + 1);
		for (Map.Entry<Integer, Head> entry : headMapData.entrySet()) {
			Head headData = entry.getValue();
			if (headData.getForceIndex() || !headData.getForceName()) {
				tmpHeadMap.put(entry.getKey(), headData);
				continue;
			}
			List<String> headNameList = headData.getHeadNameList();
			String headName = headNameList.get(headNameList.size() - 1);
			for (Map.Entry<Integer, String> stringEntry : dataMap.entrySet()) {
				if (stringEntry == null) {
					continue;
				}
				String headString = stringEntry.getValue();
				Integer stringKey = stringEntry.getKey();
				if (StringUtils.isEmpty(headString)) {
					continue;
				}
				if (analysisContext.currentReadHolder().globalConfiguration().getAutoTrim()) {
					headString = headString.trim();
				}
				if (headName.equals(headString)) {
					headData.setColumnIndex(stringKey);
					tmpHeadMap.put(stringKey, headData);
					break;
				}
			}
		}
		excelHeadPropertyData.setHeadMap(tmpHeadMap);
	}

}
