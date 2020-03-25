package com.technology.circles.apps.done.general_ui_method;

import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.technology.circles.apps.done.R;
import com.technology.circles.apps.done.tags.Tags;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class GeneralMethod {



    @BindingAdapter("error")
    public static void errorValidation(View view, String error) {
        if (view instanceof EditText) {
            EditText ed = (EditText) view;
            ed.setError(error);
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setError(error);


        }
    }

    @BindingAdapter("imageResource")
    public static void displayImageResource(View view, int resource) {
        if (view instanceof RoundedImageView) {

            RoundedImageView imageView = (RoundedImageView) view;
            imageView.setImageResource(resource);
        }
    }

    @BindingAdapter("endPoint")
    public static void displayProfileImage(View view, String endPoint) {
        if (view instanceof RoundedImageView) {

            RoundedImageView imageView = (RoundedImageView) view;
            Picasso.get().load(Uri.parse(Tags.image_path+endPoint)).placeholder(R.drawable.user_avatar).fit().into(imageView);
        }else if (view instanceof CircleImageView)
        {
            CircleImageView imageView = (CircleImageView) view;
            Picasso.get().load(Uri.parse(Tags.image_path+endPoint)).placeholder(R.drawable.user_avatar).fit().into(imageView);

        }
    }

    @BindingAdapter("date")
    public static void displayDataLocal(TextView textView,long date)
    {
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
        String d = format.format(new Date(date));
        textView.setText(d);
    }


    @BindingAdapter("time")
    public static void displayTimeLocal(TextView textView,long time)
    {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
        String t = format.format(new Date(time));
        textView.setText(t);
    }


    @BindingAdapter("hour")
    public static void displayHours(TextView textView,long time)
    {
        SimpleDateFormat format = new SimpleDateFormat("hh", Locale.ENGLISH);
        String t = format.format(new Date(time));
        textView.setText(t);
    }

    @BindingAdapter("minute")
    public static void displayMinute(TextView textView,long time)
    {
        SimpleDateFormat format = new SimpleDateFormat("mm", Locale.ENGLISH);
        String t = format.format(new Date(time));
        textView.setText(t);
    }

    @BindingAdapter("am_pm")
    public static void displayAM_PM(TextView textView,long time)
    {
        SimpleDateFormat format = new SimpleDateFormat("aa", Locale.ENGLISH);
        String t = format.format(new Date(time));
        textView.setText(t);
    }







}
