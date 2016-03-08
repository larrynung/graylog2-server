/**
 * This file is part of Graylog Pipeline Processor.
 *
 * Graylog Pipeline Processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog Pipeline Processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog Pipeline Processor.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.plugins.pipelineprocessor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageCollection;
import org.graylog2.plugin.Messages;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EvaluationContext {

    private static final EvaluationContext EMPTY_CONTEXT = new EvaluationContext() {
        @Override
        public void addCreatedMessage(Message newMessage) {
            // cannot add messages to empty context
        }

        @Override
        public void define(String identifier, Class type, Object value) {
            // cannot define any variables in empty context
        }
    };

    private final Message message;
    private Map<String, TypedValue> ruleVars;
    private List<Message> createdMessages = Lists.newArrayList();
    private List<EvalError> evalErrors;

    private EvaluationContext() {
        this(new Message("__dummy", "__dummy", DateTime.parse("2010-07-30T16:03:25Z"))); // first Graylog release
    }

    public EvaluationContext(Message message) {
        this.message = message;
        ruleVars = Maps.newHashMap();
        evalErrors = Lists.newArrayList();
    }

    public void define(String identifier, Class type, Object value) {
        ruleVars.put(identifier, new TypedValue(type, value));
    }

    public Message currentMessage() {
        return message;
    }

    public TypedValue get(String identifier) {
        return ruleVars.get(identifier);
    }

    public Messages createdMessages() {
        return new MessageCollection(createdMessages);
    }

    public void addCreatedMessage(Message newMessage) {
        createdMessages.add(newMessage);
    }

    public void clearCreatedMessages() {
        createdMessages.clear();
    }

    public static EvaluationContext emptyContext() {
        return EMPTY_CONTEXT;
    }

    public void addEvaluationError(int line, int charPositionInLine, FunctionDescriptor descriptor, Exception e) {
        evalErrors.add(new EvalError(line, charPositionInLine, descriptor, e));
    }

    public boolean hasEvaluationErrors() {
        return evalErrors.size() > 0;
    }

    public List<EvalError> evaluationErrors() {
        return Collections.unmodifiableList(evalErrors);
    }

    public class TypedValue {
        private final Class type;
        private final Object value;

        public TypedValue(Class type, Object value) {
            this.type = type;
            this.value = value;
        }

        public Class getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }

    public static class EvalError {
        private final int line;
        private final int charPositionInLine;
        private final FunctionDescriptor descriptor;
        private final Exception e;

        public EvalError(int line, int charPositionInLine, FunctionDescriptor descriptor, Exception e) {
            this.line = line;
            this.charPositionInLine = charPositionInLine;
            this.descriptor = descriptor;
            this.e = e;
        }

        @Override
        public String toString() {
            return "In call to function '" + descriptor.name() + "' at " + line + ":" + charPositionInLine +
                    " an exception was thrown: " + e.getMessage();
        }
    }
}
