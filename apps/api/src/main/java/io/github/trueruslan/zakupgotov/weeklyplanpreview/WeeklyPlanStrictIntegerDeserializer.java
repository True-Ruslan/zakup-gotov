package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

final class WeeklyPlanStrictIntegerDeserializer extends StdDeserializer<Integer> {
    WeeklyPlanStrictIntegerDeserializer() {
        super(Integer.class);
    }

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
            context.reportInputMismatch(Integer.class, "expected an integer JSON number");
        }
        return parser.getIntValue();
    }
}
