/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.presto.sql.planner.assertions;

import com.facebook.presto.Session;
import com.facebook.presto.cost.StatsProvider;
import com.facebook.presto.metadata.Metadata;
import com.facebook.presto.spi.plan.PlanNode;
import com.facebook.presto.spi.plan.TableWriterNode;
import com.facebook.presto.spi.relation.VariableReferenceExpression;
import com.facebook.presto.sql.planner.Symbol;

import java.util.List;

import static com.facebook.presto.sql.planner.assertions.MatchResult.NO_MATCH;
import static com.facebook.presto.sql.planner.assertions.MatchResult.match;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public class TableWriterMatcher
        implements Matcher
{
    private final List<String> columns;
    private final List<String> columnNames;

    public TableWriterMatcher(List<String> columns, List<String> columnNames)
    {
        this.columns = columns;
        this.columnNames = columnNames;
    }

    @Override
    public boolean shapeMatches(PlanNode node)
    {
        return node instanceof TableWriterNode;
    }

    @Override
    public MatchResult detailMatches(PlanNode node, StatsProvider stats, Session session, Metadata metadata, SymbolAliases symbolAliases)
    {
        checkState(shapeMatches(node), "Plan testing framework error: shapeMatches returned false in detailMatches in %s", this.getClass().getName());

        TableWriterNode tableWriterNode = (TableWriterNode) node;
        if (!tableWriterNode.getColumnNames().equals(columnNames)) {
            return NO_MATCH;
        }

        if (!columns.stream()
                .map(s -> Symbol.from(symbolAliases.get(s)))
                .collect(toImmutableList())
                .equals(tableWriterNode.getColumns().stream().map(VariableReferenceExpression::getName).map(Symbol::new).collect(toImmutableList()))) {
            return NO_MATCH;
        }

        return match();
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("columns", columns)
                .add("columnNames", columnNames)
                .toString();
    }
}
