package com.fu.fu_cinama_ticket_system.enums;

public enum AgeRating {
    P(0), T13(13), T16(16), T18(18);

    private final int minAge;

    AgeRating(int minAge) {
        this.minAge = minAge;
    }

    public int getMinAge() {
        return minAge;
    }
}
