package com.example.itmba2_formative;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.util.Calendar;

public class BudgetActivity extends BaseActivity {

    // UI Components
    private TextView tvBackHome;
    private EditText etDestination, etTripNotes;
    private EditText etVisaFees, etTransport, etAccommodation, etInsurance;
    private TextView tvStartDate, tvEndDate;
    private Spinner spinnerTravelers;
    private CheckBox cbSightseeing, cbHiking, cbDining, cbMuseum, cbShopping;
    private TextView tvSubtotal, tvLoyaltyDiscountAmount, tvTotalAmount;
    private LinearLayout llLoyaltyDiscount;
    private Button btnSaveTrip, btnLoadTrip;


    // Date storage
    private String startDate = "";
    private String endDate = "";

    // Calculator variables
    private double subtotal = 0.0;
    private double loyaltyDiscount = 0.0;
    private double total = 0.0;
    private boolean hasLoyaltyDiscount = false;

    // Database helper and preferences
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPrefs;
    private DecimalFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Initialize helpers
        dbHelper = DatabaseHelper.getInstance(this);
        sharedPrefs = HelperMethods.getSharedPreferences(this);
        currencyFormat = new DecimalFormat("R0.00");

        // Initialize UI components
        initializeViews();
        setupSpinner();
        setupListeners();
        checkLoyaltyDiscount();
        calculateTotal();

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.budget_coordinator), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPaddingRelative(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });
    }

    private void initializeViews() {
        // Navigation
        tvBackHome = findViewById(R.id.tv_back_home);

        // Trip details
        etDestination = findViewById(R.id.et_destination);
        etTripNotes = findViewById(R.id.et_trip_notes);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        spinnerTravelers = findViewById(R.id.spinner_travelers);

        // Activity checkboxes
        cbSightseeing = findViewById(R.id.cb_sightseeing);
        cbHiking = findViewById(R.id.cb_hiking);
        cbDining = findViewById(R.id.cb_dining);
        cbMuseum = findViewById(R.id.cb_museum);
        cbShopping = findViewById(R.id.cb_shopping);

        // Custom expenses
        etVisaFees = findViewById(R.id.et_visa_fees);
        etTransport = findViewById(R.id.et_transport);
        etAccommodation = findViewById(R.id.et_accommodation);
        etInsurance = findViewById(R.id.et_insurance);

        // Summary display
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvLoyaltyDiscountAmount = findViewById(R.id.tv_loyalty_discount_amount);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        llLoyaltyDiscount = findViewById(R.id.ll_loyalty_discount);

        // Action buttons
        btnSaveTrip = findViewById(R.id.btn_save_trip);
        btnLoadTrip = findViewById(R.id.btn_load_trip);
    }

    private void setupSpinner() {
        String[] travelerOptions = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, travelerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTravelers.setAdapter(adapter);
    }

    private void setupListeners() {
        // Back button
        tvBackHome.setOnClickListener(v -> finish());

        // Date pickers
        tvStartDate.setOnClickListener(v -> showDatePicker(true));
        tvEndDate.setOnClickListener(v -> showDatePicker(false));

        // Activity checkboxes
        cbSightseeing.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        cbHiking.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        cbDining.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        cbMuseum.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        cbShopping.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());

        // Custom expense text watchers
        setupTextWatcher(etVisaFees);
        setupTextWatcher(etTransport);
        setupTextWatcher(etAccommodation);
        setupTextWatcher(etInsurance);

        // Action buttons
        btnSaveTrip.setOnClickListener(v -> saveTrip());
        btnLoadTrip.setOnClickListener(v -> showLoadTripDialog());
    }

    private void setupTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateTotal();
            }
        });
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    if (isStartDate) {
                        startDate = date;
                        tvStartDate.setText(date);
                    } else {
                        endDate = date;
                        tvEndDate.setText(date);
                    }
                }, year, month, day);

        if (isStartDate) {
            datePickerDialog.setTitle("Select Start Date");
        } else {
            datePickerDialog.setTitle("Select End Date");
        }

        datePickerDialog.show();
    }

    private void checkLoyaltyDiscount() {
        int tripCount = sharedPrefs.getInt(AppConstants.PrefKeys.PREF_TRIP_COUNT, 0);
        hasLoyaltyDiscount = tripCount >= AppConstants.TripConstants.LOYALTY_DISCOUNT_THRESHOLD;

        if (hasLoyaltyDiscount) {
            llLoyaltyDiscount.setVisibility(View.VISIBLE);
        } else {
            llLoyaltyDiscount.setVisibility(View.GONE);
        }
    }

    private void calculateTotal() {
        // Calculate predefined activities cost
        double activitiesCost = getActivitiesCost();

        // Calculate custom expenses
        double customExpenses = 0.0;
        customExpenses += getDoubleFromEditText(etVisaFees);
        customExpenses += getDoubleFromEditText(etTransport);
        customExpenses += getDoubleFromEditText(etAccommodation);
        customExpenses += getDoubleFromEditText(etInsurance);

        // Calculate subtotal
        subtotal = activitiesCost + customExpenses;

        // Calculate loyalty discount if applicable
        loyaltyDiscount = 0.0;
        if (hasLoyaltyDiscount && subtotal > 0) {
            loyaltyDiscount = subtotal * AppConstants.TripConstants.LOYALTY_DISCOUNT_PERCENTAGE;
        }

        // Calculate final total
        total = subtotal - loyaltyDiscount;

        // Update UI
        updateSummaryDisplay();
    }

    private double getActivitiesCost() {
        double activitiesCost = 0.0;
        if (cbSightseeing.isChecked()) activitiesCost += AppConstants.TripConstants.SIGHTSEEING_COST;
        if (cbHiking.isChecked()) activitiesCost += AppConstants.TripConstants.HIKING_COST;
        if (cbDining.isChecked()) activitiesCost += AppConstants.TripConstants.DINING_COST;
        if (cbMuseum.isChecked()) activitiesCost += AppConstants.TripConstants.MUSEUM_COST;
        if (cbShopping.isChecked()) activitiesCost += AppConstants.TripConstants.SHOPPING_COST;
        return activitiesCost;
    }

    private double getDoubleFromEditText(EditText editText) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void updateSummaryDisplay() {
        tvSubtotal.setText(currencyFormat.format(subtotal));
        tvTotalAmount.setText(currencyFormat.format(total));

        if (hasLoyaltyDiscount && loyaltyDiscount > 0) {
            tvLoyaltyDiscountAmount.setText("-" + currencyFormat.format(loyaltyDiscount));
        } else {
            tvLoyaltyDiscountAmount.setText("-R0.00");
        }
    }

    private void saveTrip() {
        // Validate required fields
        if (!validateInput()) {
            return;
        }

        try {
            // Get user ID from preferences
            int userId = sharedPrefs.getInt(AppConstants.PrefKeys.KEY_USER_ID, -1);
            if (userId == -1) {
                HelperMethods.showToast(this, "Please log in to save trips");
                return;
            }

            // Prepare trip data
            String destination = etDestination.getText().toString().trim();
            String notes = etTripNotes.getText().toString().trim();
            String travelers = spinnerTravelers.getSelectedItem().toString();

            // Create comprehensive trip notes including all data
            StringBuilder fullNotes = new StringBuilder();
            fullNotes.append("Destination: ").append(destination).append("\n");
            fullNotes.append("Start Date: ").append(startDate).append("\n");
            fullNotes.append("End Date: ").append(endDate).append("\n");
            fullNotes.append("Travelers: ").append(travelers).append("\n");
            fullNotes.append("Total Cost: ").append(currencyFormat.format(total)).append("\n");

            if (hasLoyaltyDiscount && loyaltyDiscount > 0) {
                fullNotes.append("Loyalty Discount Applied: ").append(currencyFormat.format(loyaltyDiscount)).append("\n");
            }

            fullNotes.append("\nSelected Activities:\n");
            if (cbSightseeing.isChecked()) fullNotes.append("- Sightseeing (").append(currencyFormat.format(AppConstants.TripConstants.SIGHTSEEING_COST)).append(")\n");
            if (cbHiking.isChecked()) fullNotes.append("- Hiking (").append(currencyFormat.format(AppConstants.TripConstants.HIKING_COST)).append(")\n");
            if (cbDining.isChecked()) fullNotes.append("- Fine Dining (").append(currencyFormat.format(AppConstants.TripConstants.DINING_COST)).append(")\n");
            if (cbMuseum.isChecked()) fullNotes.append("- Museum Tours (").append(currencyFormat.format(AppConstants.TripConstants.MUSEUM_COST)).append(")\n");
            if (cbShopping.isChecked()) fullNotes.append("- Shopping (").append(currencyFormat.format(AppConstants.TripConstants.SHOPPING_COST)).append(")\n");

            fullNotes.append("\nCustom Expenses:\n");
            double visaFees = getDoubleFromEditText(etVisaFees);
            double transport = getDoubleFromEditText(etTransport);
            double accommodation = getDoubleFromEditText(etAccommodation);
            double insurance = getDoubleFromEditText(etInsurance);

            if (visaFees > 0) fullNotes.append("- Visa Fees: ").append(currencyFormat.format(visaFees)).append("\n");
            if (transport > 0) fullNotes.append("- Transport: ").append(currencyFormat.format(transport)).append("\n");
            if (accommodation > 0) fullNotes.append("- Accommodation: ").append(currencyFormat.format(accommodation)).append("\n");
            if (insurance > 0) fullNotes.append("- Insurance: ").append(currencyFormat.format(insurance)).append("\n");

            if (!notes.isEmpty()) {
                fullNotes.append("\nAdditional Notes:\n").append(notes);
            }

            // Prepare fields for Trips table
            int travelersCount = 1;
            try {
                travelersCount = travelers.contains("+") ? 10 : Integer.parseInt(travelers);
            } catch (Exception ignore) {}

            StringBuilder activities = getStringBuilder();

            String customExpenses =
                    "visa=" + visaFees + "|" +
                    "transport=" + transport + "|" +
                    "accommodation=" + accommodation + "|" +
                    "insurance=" + insurance;

            long result = dbHelper.addTrip(
                    userId,
                    destination,
                    startDate,
                    endDate,
                    travelersCount,
                    total,
                    activities.toString(),
                    customExpenses,
                    notes,
                    hasLoyaltyDiscount,
                    loyaltyDiscount
            );

            if (result != -1) {
                // Increment trip counter in SharedPreferences
                int currentCount = sharedPrefs.getInt(AppConstants.PrefKeys.PREF_TRIP_COUNT, 0);
                sharedPrefs.edit().putInt(AppConstants.PrefKeys.PREF_TRIP_COUNT, currentCount + 1).apply();

                // Check if user just qualified for loyalty discount
                if (currentCount + 1 == AppConstants.TripConstants.LOYALTY_DISCOUNT_THRESHOLD) {
                    HelperMethods.showToast(this, "Congratulations! You've unlocked 10% loyalty discount for future trips!");
                    checkLoyaltyDiscount();
                    calculateTotal();
                }

                HelperMethods.showToast(this, "Trip saved successfully!");
                clearForm();
            } else {
                HelperMethods.showToast(this, "Failed to save trip. Please try again.");
            }

        } catch (Exception e) {
            HelperMethods.showToast(this, "An error occurred while saving the trip");
        }
    }

    @NonNull
    private StringBuilder getStringBuilder() {
        StringBuilder activities = new StringBuilder();
        if (cbSightseeing.isChecked()) activities.append("sightseeing,");
        if (cbHiking.isChecked()) activities.append("hiking,");
        if (cbDining.isChecked()) activities.append("dining,");
        if (cbMuseum.isChecked()) activities.append("museum,");
        if (cbShopping.isChecked()) activities.append("shopping,");
        if (activities.length() > 0 && activities.charAt(activities.length() - 1) == ',') {
            activities.deleteCharAt(activities.length() - 1);
        }
        return activities;
    }

    private boolean validateInput() {
        if (HelperMethods.isEmpty(etDestination.getText().toString())) {
            HelperMethods.showToast(this, "Please enter a destination");
            etDestination.requestFocus();
            return false;
        }

        if (HelperMethods.isEmpty(startDate)) {
            HelperMethods.showToast(this, "Please select a start date");
            return false;
        }

        if (HelperMethods.isEmpty(endDate)) {
            HelperMethods.showToast(this, "Please select an end date");
            return false;
        }

        return true;
    }

    private void clearForm() {
        etDestination.setText("");
        etTripNotes.setText("");
        tvStartDate.setText("");
        tvEndDate.setText("");
        startDate = "";
        endDate = "";
        spinnerTravelers.setSelection(0);

        // Clear checkboxes
        cbSightseeing.setChecked(false);
        cbHiking.setChecked(false);
        cbDining.setChecked(false);
        cbMuseum.setChecked(false);
        cbShopping.setChecked(false);

        // Clear custom expenses
        etVisaFees.setText("");
        etTransport.setText("");
        etAccommodation.setText("");
        etInsurance.setText("");

        // Recalculate
        calculateTotal();
    }

    private void showLoadTripDialog() {
        int userId = sharedPrefs.getInt(AppConstants.PrefKeys.KEY_USER_ID, -1);
        if (userId == -1) {
            HelperMethods.showToast(this, "Please log in to load trips");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Load Trip")
                .setMessage("Load your most recent saved trip and prefill the form?")
                .setPositiveButton("Load Recent", (dialog, which) -> {
                    try {
                        com.example.itmba2_formative.objects.Trip trip = dbHelper.getMostRecentTripObject(userId);
                        if (trip != null) {
                            // Destination
                            etDestination.setText(trip.getDestination());

                            // Dates
                            startDate = trip.getStartDate();
                            endDate = trip.getEndDate();
                            tvStartDate.setText(startDate);
                            tvEndDate.setText(endDate);

                            // Travelers
                            int travelersCount = trip.getTravelersCount();
                            int spinnerIndex = Math.max(0, Math.min(travelersCount >= 10 ? 9 : travelersCount - 1, 9));
                            spinnerTravelers.setSelection(spinnerIndex);

                            // Notes
                            String notes = trip.getNotes();
                            etTripNotes.setText(notes != null ? notes : "");

                            // Activities
                            String activities = trip.getActivitiesSelected();
                            cbSightseeing.setChecked(activities != null && activities.contains("sightseeing"));
                            cbHiking.setChecked(activities != null && activities.contains("hiking"));
                            cbDining.setChecked(activities != null && activities.contains("dining"));
                            cbMuseum.setChecked(activities != null && activities.contains("museum"));
                            cbShopping.setChecked(activities != null && activities.contains("shopping"));

                            // Custom expenses
                            String expenses = trip.getCustomExpenses();
                            double visa = 0, transport = 0, acc = 0, ins = 0;
                            if (!TextUtils.isEmpty(expenses)) {
                                String[] parts = expenses.split("\\|");
                                for (String part : parts) {
                                    String[] kv = part.split("=");
                                    if (kv.length == 2) {
                                        try {
                                            double val = Double.parseDouble(kv[1]);
                                            switch (kv[0]) {
                                                case "visa": visa = val; break;
                                                case "transport": transport = val; break;
                                                case "accommodation": acc = val; break;
                                                case "insurance": ins = val; break;
                                            }
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }
                            }
                            etVisaFees.setText(visa == 0 ? "" : String.valueOf(visa));
                            etTransport.setText(transport == 0 ? "" : String.valueOf(transport));
                            etAccommodation.setText(acc == 0 ? "" : String.valueOf(acc));
                            etInsurance.setText(ins == 0 ? "" : String.valueOf(ins));

                            // Recalc totals using current loyalty status
                            checkLoyaltyDiscount();
                            calculateTotal();

                            HelperMethods.showToast(this, "Most recent trip loaded");
                        } else {
                            HelperMethods.showToast(this, "No trips found to load");
                        }
                    } catch (Exception e) {
                        HelperMethods.showToast(this, "Failed to load trip");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
