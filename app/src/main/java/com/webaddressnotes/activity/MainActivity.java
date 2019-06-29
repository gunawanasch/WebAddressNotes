package com.webaddressnotes.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.webaddressnotes.R;
import com.webaddressnotes.adapter.WebAddressAdapter;
import com.webaddressnotes.database.Database;
import com.webaddressnotes.model.WebAddress;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerWebAddress;
    private FloatingActionButton fabAdd;
    private Database db;
    private WebAddressAdapter adapter;
    private ArrayList<WebAddress> listWeb = new ArrayList<WebAddress>();
    private Cursor cur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupView();
    }

    private void setupView() {
        recyclerWebAddress = (RecyclerView) findViewById(R.id.recyclerWebAddress);
        fabAdd = (FloatingActionButton) findViewById(R.id.fab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new Database(this);
        getDataWebAddress();
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogFormWebAddress(false, 0);
            }
        });
    }

    private void getDataWebAddress() {
        ViewCompat.setNestedScrollingEnabled(recyclerWebAddress, false);
        recyclerWebAddress.setHasFixedSize(true);
        recyclerWebAddress.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listWeb.clear();
        listWeb = db.getListWebAddress();
        if(listWeb != null) {
            adapter = new WebAddressAdapter(this, listWeb);
            adapter.notifyDataSetChanged();
            recyclerWebAddress.setAdapter(adapter);
            adapter.setOnItemClickListener(new WebAddressAdapter.OnItemClickListener() {
                @Override
                public void onEditClick(int position) {
                    showDialogFormWebAddress(true, position);
                }
                @Override
                public void onDeleteClick(int position) {
                    db.deleteWebAddres(listWeb.get(position).getId());
                    getDataWebAddress();
                }
            });
        }
    }

    private void showDialogFormWebAddress(final boolean isEdit, final int position) {
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_form_web_address, null);
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
        final EditText etName = sheetView.findViewById(R.id.etName);
        final EditText etAddress = sheetView.findViewById(R.id.etAddress);
        Button btnSave = sheetView.findViewById(R.id.btnSave);

        if(isEdit) {
            etName.setText(listWeb.get(position).getName());
            etAddress.setText(listWeb.get(position).getAddress());
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etName.getText().toString().isEmpty() || etAddress.getText().toString().isEmpty()) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                    ab.setMessage("Please complete all fields.");
                    ab.setNeutralButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dlg, int sumthin) {}
                            }).show();
                }
                else {
                    if(isEdit) {
                        db.updateWebAddres(listWeb.get(position).getId(), etName.getText().toString().trim(), etAddress.getText().toString().trim());
                    }
                    else {
                        db.addWebAddress(etName.getText().toString().trim(), etAddress.getText().toString().trim());
                    }
                    mBottomSheetDialog.dismiss();
                    getDataWebAddress();
                }
            }
        });
    }

    private void exportToExcel() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Exporting Data");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
        String date = df.format(Calendar.getInstance().getTime());
        final String fileName = date+"_WebAddressNotes.xls";

        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath()+ "/WebAddressNotes");
        directory.mkdirs();
        File file = new File(directory, fileName);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));

        WritableWorkbook workbook;

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("Notes", 0);
            Label titleName = new Label(0,0, "NAME");
            Label titleAddress = new Label(1,0,"ADDRESS");
            cur = db.getAllDataToExcel();
            try {
                sheet.addCell(titleName);
                sheet.addCell(titleAddress);
                if (cur.getCount() > 0) {
                    int indexName = cur.getColumnIndex("name");
                    int indexAddress = cur.getColumnIndex("address");
                    cur.moveToFirst();
                    do {
                        String name = cur.getString(indexName);
                        String address = cur.getString(indexAddress);
                        int i = cur.getPosition()+1;

                        sheet.addCell(new Label(0, i, name));
                        sheet.addCell(new Label(1, i, address));
                        cur.moveToNext();
                    } while (!cur.isAfterLast());
                }
            }
            catch (RowsExceededException e) {
                e.printStackTrace();
            }
            catch (WriteException e) {
                e.printStackTrace();
            }
            workbook.write();

            progressDialog.dismiss();
            android.app.AlertDialog.Builder ab = new android.app.AlertDialog.Builder(this);
            ab.setMessage("Data has been exported to excel file.");
            ab.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {}
            }).show();
            try {
                workbook.close();
            }
            catch (WriteException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export_excel) {
            exportToExcel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
