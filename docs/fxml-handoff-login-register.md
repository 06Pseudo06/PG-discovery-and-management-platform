# FXML Naming & Binding Contract: Login and Registration Screens

This document defines the exact `fx:id` values and element types the Java controllers expect. You can design the visual layout, styling, spacing, and arrangement of `Login.fxml` and `Register.fxml` however you want — colors, fonts, positioning are entirely your call — but every element listed below must keep its exact `fx:id` and be the specified JavaFX control type, or the controller will fail at runtime with a null pointer error that's hard to trace back to a naming mismatch.

---

## 1. Login.fxml Naming Contract

**Controller Class:** `com.pgfinder.controller.LoginController`

| Element `fx:id` | JavaFX Control Type | Purpose / Expected Behavior |
| :--- | :--- | :--- |
| `emailField` | `TextField` | Input field for user's email address. |
| `passwordField` | `PasswordField` | Input field for user's password (masked). |
| `loginButton` | `Button` | Triggers the login action. Must have `onAction="#handleLogin"` defined in FXML. |
| `errorLabel` | `Label` | Label used for displaying validation or authentication error messages. |
| `goToRegisterLink` | `Hyperlink` or `Button` | Navigates to the Registration screen. Must have `onAction="#handleGoToRegister"` defined. |

---

## 2. Register.fxml Naming Contract

**Controller Class:** `com.pgfinder.controller.RegisterController`

| Element `fx:id` | JavaFX Control Type | Purpose / Expected Behavior |
| :--- | :--- | :--- |
| `nameField` | `TextField` | Input field for the user's full name. |
| `emailField` | `TextField` | Input field for the user's email address. |
| `passwordField` | `PasswordField` | Input field for the chosen password. |
| `confirmPasswordField` | `PasswordField` | Input field to confirm the password. |
| `roleComboBox` | `ComboBox<String>` | Role selector. Must be populated with exactly the strings `STUDENT` and `OWNER` as selectable options, nothing else, since the controller passes this value directly to registration logic. |
| `phoneField` | `TextField` | Input field for the user's phone number. |
| `registerButton` | `Button` | Triggers the registration action. Must have `onAction="#handleRegister"` defined in FXML. |
| `errorLabel` | `Label` | Label used for displaying validation or duplicate-email error messages. |
| `goToLoginLink` | `Hyperlink` or `Button` | Navigates back to the Login screen. Must have `onAction="#handleGoToLogin"` defined. |

---

## Important Designer Guidelines

> [!IMPORTANT]
> **Controller Bindings (`fx:controller`)**
> The root element in `Login.fxml` must define `fx:controller="com.pgfinder.controller.LoginController"`.
> The root element in `Register.fxml` must define `fx:controller="com.pgfinder.controller.RegisterController"`.
> If you recreate the FXML files fresh in Scene Builder or another visual design tool, double check that these controller bindings are re-applied, as design software can sometimes drop them.

> [!TIP]
> **Reference Implementations Available**
> The existing temporary `Login.fxml` and `Register.fxml` files in `src/main/resources/fxml/` are working reference implementations.
> You can open them directly in Scene Builder, see how they are structured and bound, and visually redesign them. It is **100% safe** to change the parent layout types (e.g. from `VBox` to `AnchorPane`, `GridPane`, etc.), colors, spacing, and styling. The only constraints you must maintain are the control types, `fx:id` names, and `onAction` method mappings listed in the tables above.
