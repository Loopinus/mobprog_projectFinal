package com.panson.mobprog.healthyfit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements IShareable{

    private DrawerLayout mDrawerLayout;
    private ArrayList<Weight> _weights;
    private ArrayList<Weight> _reversed_weights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        InitUI();
    }

    public void InitUI(){
        NavigationSetUp();
        CreateToolbar();
        _weights = HomeActivity.get_weights();
        InitSpinner();
        InitRecycler(_weights);
    }

    public void InitSpinner(){
        _reversed_weights = new ArrayList<>(_weights);
        Collections.reverse(_reversed_weights);
        Spinner spinner = findViewById(R.id.sort_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.history_sorting,R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
                            case 0: InitRecycler(_weights);
                                break;
                            case 1: InitRecycler(_reversed_weights);
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );
    }

    public void InitRecycler(ArrayList<Weight> weights){
        RecyclerView rv = findViewById(R.id.HistoryRecyclerView);
        HistoryListAdapter adapter = new HistoryListAdapter(this, weights);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    public void NavigationSetUp(){
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        TextView headerName = headerView.findViewById(R.id.tvHeaderName);
        if (PreferenceManager.get_name() == ""){headerName.setText(R.string.nav_header_error);}
        else {
            String text = this.getText(R.string.nav_welcome).toString();
            headerName.setText(String.format(Locale.ENGLISH, text, PreferenceManager.get_name()));
        }

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        switch (menuItem.getItemId()){
                            case R.id.nav_home:
                                Intent home = new Intent(getBaseContext(),HomeActivity.class);
                                startActivity(home);
                                break;
                            case R.id.nav_converter:
                                Intent body = new Intent(getBaseContext(),BodyCalculatorActivity.class);
                                startActivity(body);
                                break;
                            case R.id.nav_bmi_calc:
                                Intent bmi = new Intent(getBaseContext(),BMIActivity.class);
                                startActivity(bmi);
                                break;
                            case R.id.nav_history:
                                Intent history = new Intent(getBaseContext(),HistoryActivity.class);
                                startActivity(history);
                                break;
                            case R.id.nav_about:
                                Intent about = new Intent(getBaseContext(),AboutActivity.class);
                                startActivity(about);
                                break;
                        }
                        return false;
                    }
                });
    }

    public void CreateToolbar(){
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        actionbar.setTitle("Weight History");
    }

    public void Share(){
        String result = "";
        String date;
        double weight;
        String strWeight;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");

        for (Weight w : _weights){
            date = sdf.format(new Date(w.get_date()));
            weight = w.get_weight();

            if (!PreferenceManager.is_metric()){
                weight = Calc.KgToPound(weight);
            }

            strWeight = String.format(Locale.US, "%.1f", weight);
            result += String.format(Locale.US, "%s | %s%s\n", date, strWeight,PreferenceManager.get_weightUnit());
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, result);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_title)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        if(this instanceof IShareable) {
            inflater.inflate(R.menu.action_bar, menu);
        } else {
            inflater.inflate(R.menu.action_bar_no_share, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            case R.id.action_share:
                Share();
                return true;

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
