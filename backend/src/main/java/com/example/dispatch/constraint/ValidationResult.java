package com.example.dispatch.constraint;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid;
    private List<String> violations;

    public ValidationResult() {
        this.valid = true;
        this.violations = new ArrayList<>();
    }

    public void addViolation(String message) {
        this.valid = false;
        this.violations.add(message);
    }

    public boolean isValid() { return valid; }
    public List<String> getViolations() { return violations; }
}
