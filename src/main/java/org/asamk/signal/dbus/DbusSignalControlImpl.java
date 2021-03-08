package org.asamk.signal.dbus;

import org.asamk.Signal;
import org.asamk.signal.commands.SignalCreator;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.ProvisioningManager;
import org.asamk.signal.manager.RegistrationManager;
import org.asamk.signal.manager.UserAlreadyExists;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.signalservice.api.KeyBackupServicePinException;
import org.whispersystems.signalservice.api.KeyBackupSystemNoDataException;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class DbusSignalControlImpl implements org.asamk.SignalControl {

    private final SignalCreator c;
    private final Consumer<Manager> newManagerHandler;

    public DbusSignalControlImpl(final SignalCreator c, final Consumer<Manager> newManagerHandler) {
        this.c = c;
        this.newManagerHandler = newManagerHandler;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String getObjectPath() {
        return null;
    }

    @Override
    public void register(
            final String number, final boolean voiceVerification
    ) throws Error.Failure, Error.InvalidNumber {
        registerWithCaptcha(number, voiceVerification, null);
    }

    @Override
    public void registerWithCaptcha(
            final String number, final boolean voiceVerification, final String captcha
    ) throws Error.Failure, Error.InvalidNumber {
        try (final RegistrationManager registrationManager = c.getNewRegistrationManager(number)) {
            registrationManager.register(voiceVerification, captcha);
        } catch (IOException e) {
            throw new Signal.Error.Failure(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    @Override
    public void verify(final String number, final String verificationCode) throws Error.Failure, Error.InvalidNumber {
        verifyWithPin(number, verificationCode, null);
    }

    @Override
    public void verifyWithPin(
            final String number, final String verificationCode, final String pin
    ) throws Error.Failure, Error.InvalidNumber {
        try (final RegistrationManager registrationManager = c.getNewRegistrationManager(number)) {
            final Manager manager = registrationManager.verifyAccount(verificationCode, pin);
            newManagerHandler.accept(manager);
        } catch (IOException | KeyBackupSystemNoDataException | KeyBackupServicePinException e) {
            throw new Signal.Error.Failure(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    @Override
    public String link(final String newDeviceName) throws Error.Failure {
        try {
            final ProvisioningManager provisioningManager = c.getNewProvisioningManager();
            final URI deviceLinkUri = provisioningManager.getDeviceLinkUri();
            new Thread(() -> {
                try {
                    final Manager manager = provisioningManager.finishDeviceLink(newDeviceName);
                    newManagerHandler.accept(manager);
                } catch (IOException | InvalidKeyException | TimeoutException | UserAlreadyExists e) {
                    e.printStackTrace();
                }
            }).start();
            return deviceLinkUri.toString();
        } catch (TimeoutException | IOException e) {
            throw new Signal.Error.Failure(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }
}
