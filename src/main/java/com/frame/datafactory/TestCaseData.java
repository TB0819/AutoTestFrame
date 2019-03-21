package com.frame.datafactory;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * 测试用例对象
 */
public class TestCaseData implements Iterator<Object[]> {
    protected static final Logger logger = Logger.getLogger(TestCaseData.class);
    protected int rowNum = 0;
    protected int currentRowNo = 0;
    protected int columnNum = 0;
    protected String[] columnName;
    protected List tableList;

    /**
     * 获取用例个数
     * @return
     */
    public int getCaseCount() {
        return this.rowNum - 1;
    }

    @Override
    public boolean hasNext() {
        if (this.rowNum == 0 || this.currentRowNo >= this.rowNum) {
            return false;
        }
        return true;
    }

    @Override
    public Object[] next() {
        Map<String, String> data = new LinkedHashMap<String, String>();
        String nextLine[] = (String[]) tableList.get(currentRowNo);
        List<String> keys = Arrays.asList(nextLine);
        if (keys.size() > this.columnNum) {
            logger.error("当前行的列数大于csv文件中第一列的个数，请仔细核对");
            System.exit(0);
        }

        for (int i = 0; i < this.columnNum; i++) {
            String temp;
            try {
                temp = String.valueOf(nextLine[i]);
            } catch (ArrayIndexOutOfBoundsException ex) {
                temp = "";
            }
            if(null == this.columnName[i] || this.columnName[i].isEmpty())
                continue;
            data.put(this.columnName[i], temp);
        }
        Object[] objectArr = new Object[1];
        objectArr[0] = data;

        this.currentRowNo++;
        return objectArr;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
