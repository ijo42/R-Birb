package ru.ijo42.rbirb.tgbot.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface BotCommand {
    /**
     * Returns an array of the commands supported by handler
     *
     * @return an array of the commands supported by handler
     */
    String[] command();

    /**
     * Returns help message for the command
     *
     * @return help message for the command
     */
    String message() default "";
}
