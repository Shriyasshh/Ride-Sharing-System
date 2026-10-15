package com.ridesharing.ui.app;

import com.ridesharing.service.ServiceFactory;
import com.ridesharing.ui.navigation.HeaderBar;
import com.ridesharing.ui.navigation.Sidebar;
import com.ridesharing.ui.screens.*;
import com.ridesharing.ui.theme.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application frame that manages authentication flow and
 * the main application shell with sidebar navigation.
 */
public class MainFrame extends JFrame {

    // ── Auth screens (shown before login) ──
    private final CardLayout authLayout = new CardLayout();
    private final JPanel authPanel = new JPanel(authLayout);

    // ── App screens (shown after login) ──
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);

    // ── Top-level card layout (auth vs app) ──
    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootLayout);

    // ── Navigation ──
    private Sidebar sidebar;
    private HeaderBar headerBar;

    // ── Screens ──
    private WelcomeScreen welcomeScreen;
    private LoginScreen loginScreen;
    private RegisterScreen registerScreen;

    private DashboardScreen dashboardScreen;
    private FindRidesScreen findRidesScreen;
    private PublishRideScreen publishRideScreen;
    private MyRidesScreen myRidesScreen;
    private MyBookingsScreen myBookingsScreen;
    private MyVehiclesScreen myVehiclesScreen;
    private ProfileScreen profileScreen;
    private SettingsScreen settingsScreen;

    // ── Screen title mapping ──
    private final Map<String, String> screenTitles = new HashMap<>();

    public MainFrame() {
        setTitle("RideShare — Ride Sharing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null);

        initScreenTitles();
        buildAuthScreens();
        buildAppShell();

        rootPanel.add(authPanel, "auth");
        rootPanel.add(buildAppPanel(), "app");

        setContentPane(rootPanel);
        showAuth();
    }

    private void initScreenTitles() {
        screenTitles.put("dashboard", "Dashboard");
        screenTitles.put("find_rides", "Find Rides");
        screenTitles.put("publish_ride", "Publish a Ride");
        screenTitles.put("my_rides", "My Published Rides");
        screenTitles.put("my_bookings", "My Bookings");
        screenTitles.put("my_vehicles", "My Vehicles");
        screenTitles.put("profile", "Profile");
        screenTitles.put("settings", "Settings");
    }

    // ── Auth Flow ──

    private void buildAuthScreens() {
        welcomeScreen = new WelcomeScreen();
        welcomeScreen.setOnLogin(() -> authLayout.show(authPanel, "login"));
        welcomeScreen.setOnRegister(() -> authLayout.show(authPanel, "register"));

        loginScreen = new LoginScreen();
        loginScreen.setOnSuccess(this::onLoginSuccess);
        loginScreen.setOnBack(() -> authLayout.show(authPanel, "welcome"));

        registerScreen = new RegisterScreen();
        registerScreen.setOnSuccess(this::onLoginSuccess);
        registerScreen.setOnBack(() -> authLayout.show(authPanel, "welcome"));

        authPanel.add(welcomeScreen, "welcome");
        authPanel.add(loginScreen, "login");
        authPanel.add(registerScreen, "register");
    }

    private void showAuth() {
        loginScreen.reset();
        registerScreen.reset();
        rootLayout.show(rootPanel, "auth");
        authLayout.show(authPanel, "welcome");
    }

    private void onLoginSuccess() {
        rootLayout.show(rootPanel, "app");
        navigateTo("dashboard");
        headerBar.refreshUser();
    }

    // ── App Shell ──

    private void buildAppShell() {
        dashboardScreen = new DashboardScreen();
        dashboardScreen.setOnFindRides(() -> navigateTo("find_rides"));
        dashboardScreen.setOnPublishRide(() -> navigateTo("publish_ride"));

        findRidesScreen = new FindRidesScreen();
        publishRideScreen = new PublishRideScreen();
        publishRideScreen.setOnPublished(() -> navigateTo("my_rides"));

        myRidesScreen = new MyRidesScreen();
        myBookingsScreen = new MyBookingsScreen();
        myVehiclesScreen = new MyVehiclesScreen();
        profileScreen = new ProfileScreen();
        settingsScreen = new SettingsScreen();
        settingsScreen.setOnLogout(this::doLogout);

        contentPanel.add(dashboardScreen, "dashboard");
        contentPanel.add(findRidesScreen, "find_rides");
        contentPanel.add(publishRideScreen, "publish_ride");
        contentPanel.add(myRidesScreen, "my_rides");
        contentPanel.add(myBookingsScreen, "my_bookings");
        contentPanel.add(myVehiclesScreen, "my_vehicles");
        contentPanel.add(profileScreen, "profile");
        contentPanel.add(settingsScreen, "settings");
    }

    private JPanel buildAppPanel() {
        JPanel appPanel = new JPanel(new BorderLayout());

        // Sidebar
        sidebar = new Sidebar();
        sidebar.setNavigationListener(this::navigateTo);
        sidebar.setOnLogout(this::doLogout);

        // Header
        headerBar = new HeaderBar();
        headerBar.setHeaderListener(new HeaderBar.HeaderListener() {
            @Override
            public void onNavigate(String screenName) {
                navigateTo(screenName);
            }

            @Override
            public void onLogout() {
                doLogout();
            }
        });

        // Content area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(headerBar, BorderLayout.NORTH);
        centerPanel.add(contentPanel, BorderLayout.CENTER);

        appPanel.add(sidebar, BorderLayout.WEST);
        appPanel.add(centerPanel, BorderLayout.CENTER);

        return appPanel;
    }

    private void navigateTo(String screenName) {
        sidebar.navigateTo(screenName);
        contentLayout.show(contentPanel, screenName);
        headerBar.setPageTitle(screenTitles.getOrDefault(screenName, "RideShare"));
        headerBar.refreshUser();

        // Refresh screen data
        switch (screenName) {
            case "dashboard" -> dashboardScreen.refresh();
            case "find_rides" -> findRidesScreen.refresh();
            case "publish_ride" -> publishRideScreen.refresh();
            case "my_rides" -> myRidesScreen.refresh();
            case "my_bookings" -> myBookingsScreen.refresh();
            case "my_vehicles" -> myVehiclesScreen.refresh();
            case "profile" -> profileScreen.refresh();
            case "settings" -> settingsScreen.refresh();
        }
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            ServiceFactory.getAuthService().logout();
            showAuth();
        }
    }
}
