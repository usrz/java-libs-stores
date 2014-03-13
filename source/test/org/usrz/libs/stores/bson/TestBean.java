/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.stores.bson;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class TestBean {

    private String theString;
    private Date theDate;
    private int theInteger;
    private Double theDouble;
    private NestedBean nested;

    public TestBean() {
        nested = new NestedBean();
        nested.setValue("Initial value for nested bean");
    }

    public String getTheString() {
        return theString;
    }

    public void setTheString(String theString) {
        this.theString = theString;
    }

    public Date getTheDate() {
        return theDate;
    }

    public void setTheDate(Date theDate) {
        this.theDate = theDate;
    }

    public int getTheInteger() {
        return theInteger;
    }

    public void setTheInteger(int theInteger) {
        this.theInteger = theInteger;
    }

    public Double getTheDouble() {
        return theDouble;
    }

    public void setTheDouble(Double theDouble) {
        this.theDouble = theDouble;
    }


    public NestedBean getNested() {
        return nested;
    }

    public void setNested(NestedBean nested) {
        this.nested = nested;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        try {
            TestBean bean = (TestBean) object;
            return theString  == null ? bean.theString == null : theString.equals(bean.theString) &&
                   theDate    == null ? bean.theDate   == null : theDate  .equals(bean.theDate  ) &&
                   theDouble  == null ? bean.theDouble == null : theDouble.equals(bean.theDouble) &&
                   nested     == null ? bean.nested    == null : nested   .equals(bean.nested   ) &&
                   theInteger == bean.theInteger;
        } catch (ClassCastException exception) {
            return false;
        }
    }

    public static class NestedBean {

        private byte[] data = new byte[128];
        private String value;

        public NestedBean() {
            new Random().nextBytes(data);
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null) return false;
            if (object == this) return true;
            try {
                NestedBean bean = (NestedBean) object;
                return value == null ? bean.value == null : value.equals(bean.value) &&
                       data  == null ? bean.data  == null : Arrays.equals(data, bean.data);
            } catch (ClassCastException exception) {
                return false;
            }
        }

    }

}
