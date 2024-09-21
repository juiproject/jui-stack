/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.rpc.handler.client.query;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecordResultTest {

    @Test
    public void test_sameness() {
        TestRecordResult v1 = new TestRecordResult();
        v1.setId(1L);
        v1.setVersion(1);

        TestRecordResult v2 = new TestRecordResult();
        v2.setId(1L);
        v2.setVersion(1);

        TestRecordResult v3 = new TestRecordResult();
        v3.setId(1L);
        v3.setVersion(2);

        TestRecordResult v4 = new TestRecordResult();
        v4.setId(2L);
        v4.setVersion(1);

        // V1 and V2 are both equal and the same (equality -> sameness).
        Assertions.assertTrue (v1.equals (v2));
        Assertions.assertTrue (v1.same (v2));

        // V1 and V3 are not equal but are the same (the ID's match).
        Assertions.assertFalse (v1.equals (v3));
        Assertions.assertTrue (v1.same (v3));

        // V1 and V4 are neither the same or eqaul (~same -> ~equal).
        Assertions.assertFalse (v1.equals (v4));
        Assertions.assertFalse (v1.same (v4));
    }

    public void test_noversion() {
        TestRecordResult v1 = new TestRecordResult();
        v1.setId(1L);

        Assertions.assertEquals (-1, v1.getVersion());
    }

    static class TestRecordResult extends RecordResult<Long> {}
}
