package org.khawaja.fileshare.client.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

public class VCardActivity
{

    Cursor cursor;
    ArrayList<String> vCard ;


    public void getVCF(Context mContext) {
        final String vfile = "Contacts.vcf";
        Cursor phones = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null);
        phones.moveToFirst();
        for (int i = 0; i < phones.getCount(); i++) {
            String lookupKey = phones.getString(phones
                    .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_VCARD_URI,
                    lookupKey);
            AssetFileDescriptor fd;
            try {
                fd = mContext.getContentResolver().openAssetFileDescriptor(uri, "r");
                FileInputStream fis = fd.createInputStream();
                byte[] buf = new byte[(int) fd.getDeclaredLength()];
                fis.read(buf);
                String VCard = new String(buf);
                String path = Environment.getExternalStorageDirectory()
                        .toString() + File.separator + vfile;
                FileOutputStream mFileOutputStream = new FileOutputStream(path,
                        true);
                mFileOutputStream.write(VCard.toString().getBytes());
                phones.moveToNext();
                Log.d("Vcard", VCard);
                Log.d("Storage_path_vcf", path.toString());
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }



    public void getVcardString(Context context, String vfile) {
        // TODO Auto-generated method stub
        vCard = new ArrayList<String>();
        cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if(cursor!=null&&cursor.getCount()>0)
        {
            cursor.moveToFirst();
            for(int i =0;i<cursor.getCount();i++)
            {

                get(cursor, context, vfile);
                //Log.d("TAG", "Contact "+(i+1)+"VcF String is"+vCard.get(i));

                Log.d("Storage_path_vcf", "  "+(i+1)+"  "+vCard.toString());

                cursor.moveToNext();
            }

        }
        else
        {
            Log.d("TAG", "No Contacts in Your Phone");
        }

    }

    public void get(Cursor cursor, Context context, String vfile)
    {


        //cursor.moveToFirst();
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        Log.d("Storage_path_vcf_path", " uri "+ uri.toString());
        AssetFileDescriptor fd;
        try {
            fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");

            Log.d("Storage_path_vcf_path", "  "+ "alive 1");

            // Your Complex Code and you used function without loop so how can you get all Contacts Vcard.??


           /* FileInputStream fis = fd.createInputStream();
            byte[] buf = new byte[(int) fd.getDeclaredLength()];
            fis.read(buf);
            String VCard = new String(buf);
            String path = Environment.getExternalStorageDirectory().toString() + File.separator + vfile;
            FileOutputStream out = new FileOutputStream(path);
            out.write(VCard.toString().getBytes());
            Log.d("Vcard",  VCard);*/

            FileInputStream fis = fd.createInputStream();
            Log.d("Storage_path_vcf_path", "  "+ "alive star");
            byte[] buf = new byte[(int) fd.getDeclaredLength()];
            Log.d("Storage_path_vcf_path", "  "+ "alive star2");
            fis.read(buf);
            Log.d("Storage_path_vcf_path", "  "+ "alive star3");
            String vcardstring= new String(buf);
            Log.d("Storage_path_vcf_path", "  "+ "alive 2");
            vCard.add(vcardstring);

            Log.d("Storage_path_vcf_path", "  "+ "alive 3");

            String storage_path = Environment.getExternalStorageDirectory().toString() + File.separator + vfile;
            FileOutputStream mFileOutputStream = new FileOutputStream(storage_path, false);

            Log.d("Storage_path_vcf_path_", "  "+ storage_path);

            mFileOutputStream.write(vcardstring.toString().getBytes());

        } catch (Exception e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();

            Log.d("Storage_path_vcf_path", "  "+ "exception");
        }
    }


}