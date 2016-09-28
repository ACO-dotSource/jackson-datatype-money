package org.zalando.jackson.datatype.money;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.javamoney.moneta.Money;
import org.junit.Test;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.zalando.jackson.datatype.money.FieldNames.defaults;

public final class MonetaryAmountSerializerTest {

    @Test
    public void shouldSerialize() throws JsonProcessingException {
        final ObjectMapper unit = new ObjectMapper().findAndRegisterModules();

        final String expected = "{\"amount\":29.95,\"currency\":\"EUR\"}";
        final String actual = unit.writeValueAsString(Money.of(29.95, "EUR"));

        assertThat(actual, is(expected));
    }
    
    @Test
    public void defaultConstructorShouldFallbackToNoFormatting() throws IOException {
        final ObjectMapper unit = new ObjectMapper().registerModule(new SimpleModule()
                .addSerializer(CurrencyUnit.class, new CurrencyUnitSerializer())
                .addSerializer(MonetaryAmount.class, new MonetaryAmountSerializer()));

        final String expected = "{\"amount\":29.95,\"currency\":\"EUR\"}";
        final String actual = unit.writeValueAsString(Money.of(29.95, "EUR"));

        assertThat(actual, is(expected));
    }
    
    @Test
    public void shouldSerializeWithoutFormattedValueIfFactoryProducesNull() throws JsonProcessingException {
        final ObjectMapper unit = new ObjectMapper()
                .registerModule(new MoneyModule());
        
        final String expected = "{\"amount\":29.95,\"currency\":\"EUR\"}";
        final String actual = unit.writeValueAsString(Money.of(29.95, "EUR"));

        assertThat(actual, is(expected));
    }

    @Test
    @SuppressWarnings("deprecation") // TODO switch to #withFormatFactory after removing the constructor
    public void shouldSerializeWithFormattedGermanValue() throws JsonProcessingException {
        final ObjectMapper unit = new ObjectMapper()
                .registerModule(new MoneyModule(new DefaultMonetaryAmountFormatFactory()));

        final String expected = "{\"amount\":29.95,\"currency\":\"EUR\",\"formatted\":\"29,95 EUR\"}";

        final ObjectWriter writer = unit.writer().with(Locale.GERMANY);
        final String actual = writer.writeValueAsString(Money.of(29.95, "EUR"));

        assertThat(actual, is(expected));
    }

    @Test
    public void shouldSerializeWithFormattedAmericanValue() throws JsonProcessingException {
        final ObjectMapper unit = new ObjectMapper()
                .registerModule(new MoneyModule().withFormatFactory(new DefaultMonetaryAmountFormatFactory()));

        final String expected = "{\"amount\":29.95,\"currency\":\"USD\",\"formatted\":\"USD29.95\"}";

        final ObjectWriter writer = unit.writer().with(Locale.US);
        final String actual = writer.writeValueAsString(Money.of(29.95, "USD"));

        assertThat(actual, is(expected));
    }

    @Test
    public void shouldSerializeWithCustomName() throws IOException {
        final ObjectMapper unit = new ObjectMapper()
                .registerModule(new MoneyModule().withFormatFactory(new DefaultMonetaryAmountFormatFactory())
                        .withFieldNames(defaults()
                                .withAmount("value")
                                .withCurrency("unit")
                                .withFormatted("pretty")));

        final String expected = "{\"value\":29.95,\"unit\":\"EUR\",\"pretty\":\"29,95 EUR\"}";

        final ObjectWriter writer = unit.writer().with(Locale.GERMANY);
        final String actual = writer.writeValueAsString(Money.of(29.95, "EUR"));

        assertThat(actual, is(expected));
    }

}