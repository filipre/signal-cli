package org.asamk;

import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * DBus interface for the org.asamk.SignalControl interface.
 * Including emitted Signals and returned Errors.
 */
public interface SignalControl extends DBusInterface {

    void register(String number, boolean voiceVerification) throws Error.Failure, Error.InvalidNumber;

    void registerWithCaptcha(
            String number, boolean voiceVerification, String captcha
    ) throws Error.Failure, Error.InvalidNumber;

    void verify(String number, String verificationCode) throws Error.Failure, Error.InvalidNumber;

    void verifyWithPin(String number, String verificationCode, String pin) throws Error.Failure, Error.InvalidNumber;

    String link(String newDeviceName) throws Error.Failure;

    interface Error {

        class Failure extends DBusExecutionException {

            public Failure(final String message) {
                super(message);
            }
        }

        class InvalidNumber extends DBusExecutionException {

            public InvalidNumber(final String message) {
                super(message);
            }
        }
    }
}
