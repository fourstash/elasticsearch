/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.index.query;

import org.apache.lucene.queries.spans.SpanWithinQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.test.AbstractQueryTestCase;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;

public class SpanWithinQueryBuilderTests extends AbstractQueryTestCase<SpanWithinQueryBuilder> {
    @Override
    protected SpanWithinQueryBuilder doCreateTestQueryBuilder() {
        SpanTermQueryBuilder[] spanTermQueries = new SpanTermQueryBuilderTests().createSpanTermQueryBuilders(2);
        return new SpanWithinQueryBuilder(spanTermQueries[0], spanTermQueries[1]);
    }

    @Override
    protected void doAssertLuceneQuery(SpanWithinQueryBuilder queryBuilder, Query query, SearchExecutionContext context) {
        assertThat(query, instanceOf(SpanWithinQuery.class));
    }

    public void testIllegalArguments() {
        SpanTermQueryBuilder spanTermQuery = new SpanTermQueryBuilder("field", "value");
        expectThrows(IllegalArgumentException.class, () -> new SpanWithinQueryBuilder(null, spanTermQuery));
        expectThrows(IllegalArgumentException.class, () -> new SpanWithinQueryBuilder(spanTermQuery, null));
    }

    public void testFromJson() throws IOException {
        String json = "{\n"
            + "  \"span_within\" : {\n"
            + "    \"big\" : {\n"
            + "      \"span_near\" : {\n"
            + "        \"clauses\" : [ {\n"
            + "          \"span_term\" : {\n"
            + "            \"field1\" : {\n"
            + "              \"value\" : \"bar\",\n"
            + "              \"boost\" : 1.0\n"
            + "            }\n"
            + "          }\n"
            + "        }, {\n"
            + "          \"span_term\" : {\n"
            + "            \"field1\" : {\n"
            + "              \"value\" : \"baz\",\n"
            + "              \"boost\" : 1.0\n"
            + "            }\n"
            + "          }\n"
            + "        } ],\n"
            + "        \"slop\" : 5,\n"
            + "        \"in_order\" : true,\n"
            + "        \"boost\" : 1.0\n"
            + "      }\n"
            + "    },\n"
            + "    \"little\" : {\n"
            + "      \"span_term\" : {\n"
            + "        \"field1\" : {\n"
            + "          \"value\" : \"foo\",\n"
            + "          \"boost\" : 1.0\n"
            + "        }\n"
            + "      }\n"
            + "    },\n"
            + "    \"boost\" : 2.0\n"
            + "  }\n"
            + "}";

        SpanWithinQueryBuilder parsed = (SpanWithinQueryBuilder) parseQuery(json);
        checkGeneratedJson(json, parsed);

        assertEquals(json, "foo", ((SpanTermQueryBuilder) parsed.littleQuery()).value());
        assertEquals(json, 2, ((SpanNearQueryBuilder) parsed.bigQuery()).clauses().size());
        assertEquals(json, 2.0, parsed.boost(), 0.0);
    }

    public void testFromJsonWithNonDefaultBoostInBigQuery() {
        String json = "{\n"
            + "  \"span_within\" : {\n"
            + "    \"big\" : {\n"
            + "      \"span_near\" : {\n"
            + "        \"clauses\" : [ {\n"
            + "          \"span_term\" : {\n"
            + "            \"field1\" : {\n"
            + "              \"value\" : \"bar\",\n"
            + "              \"boost\" : 1.0\n"
            + "            }\n"
            + "          }\n"
            + "        }, {\n"
            + "          \"span_term\" : {\n"
            + "            \"field1\" : {\n"
            + "              \"value\" : \"baz\",\n"
            + "              \"boost\" : 1.0\n"
            + "            }\n"
            + "          }\n"
            + "        } ],\n"
            + "        \"slop\" : 5,\n"
            + "        \"in_order\" : true,\n"
            + "        \"boost\" : 2.0\n"
            + "      }\n"
            + "    },\n"
            + "    \"little\" : {\n"
            + "      \"span_term\" : {\n"
            + "        \"field1\" : {\n"
            + "          \"value\" : \"foo\",\n"
            + "          \"boost\" : 1.0\n"
            + "        }\n"
            + "      }\n"
            + "    },\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}";

        Exception exception = expectThrows(ParsingException.class, () -> parseQuery(json));
        assertThat(exception.getMessage(), equalTo("span_within [big] as a nested span clause can't have non-default boost value [2.0]"));
    }

    public void testFromJsonWithNonDefaultBoostInLittleQuery() {
        String json = "{\n"
            + "  \"span_within\" : {\n"
            + "    \"little\" : {\n"
            + "      \"span_near\" : {\n"
            + "        \"clauses\" : [ {\n"
            + "          \"span_term\" : {\n"
            + "            \"field1\" : {\n"
            + "              \"value\" : \"bar\",\n"
            + "              \"boost\" : 1.0\n"
            + "            }\n"
            + "          }\n"
            + "        }, {\n"
            + "          \"span_term\" : {\n"
            + "            \"field1\" : {\n"
            + "              \"value\" : \"baz\",\n"
            + "              \"boost\" : 1.0\n"
            + "            }\n"
            + "          }\n"
            + "        } ],\n"
            + "        \"slop\" : 5,\n"
            + "        \"in_order\" : true,\n"
            + "        \"boost\" : 2.0\n"
            + "      }\n"
            + "    },\n"
            + "    \"big\" : {\n"
            + "      \"span_term\" : {\n"
            + "        \"field1\" : {\n"
            + "          \"value\" : \"foo\",\n"
            + "          \"boost\" : 1.0\n"
            + "        }\n"
            + "      }\n"
            + "    },\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}";

        Exception exception = expectThrows(ParsingException.class, () -> parseQuery(json));
        assertThat(
            exception.getMessage(),
            equalTo("span_within [little] as a nested span clause can't have non-default boost value [2.0]")
        );
    }
}
