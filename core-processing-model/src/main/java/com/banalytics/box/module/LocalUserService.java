package com.banalytics.box.module;

public interface LocalUserService {
    /**
     * @return true if password valid, otherwise false
     */
    boolean verifyPassword(String password);

    /**
     * Method checks that old password correct and set new password
     */
    void changePassword(String oldPassword, String newPassword);

    void resetPassword(String newPassword);
}
