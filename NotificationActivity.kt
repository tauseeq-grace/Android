
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.example.researchcode.databinding.ActivityNotificationBinding
import java.util.Calendar
import kotlin.random.Random

class NotificationActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNotificationBinding.inflate(layoutInflater) }
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.setTime.setOnClickListener {
            val random = Random.nextInt()
            Log.d("Notification", "onCreate: $random")
            requestRuntimePermissions(random)
        }

        // Request runtime permissions

    }

    private fun requestRuntimePermissions(random: Int) {
        // Check if the notification permission is granted
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            // You can show a dialog or launch an activity to request permission here
            // For simplicity, we'll just log a message
            println("Notification permission not granted")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            createNotificationChannel()
            showDateTimePicker(random)
        }
    }

    private fun showDateTimePicker(random: Int) {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Show time picker after selecting the date
                showTimePicker(calendar,random)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar, random: Int) {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute

                // Set the alarm with the selected date and time
                setAlarm(calendar, hourOfDay, minute,random)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timePickerDialog.show()
    }

    private fun setAlarm(calendar: Calendar, hourOfDay: Int, minute: Int, random: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                this,
                random,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        // Set the alarm at the selected date and time
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND,0)
        Log.d("Notification", "HOUR_OF_DAY: $hourOfDay")
        Log.d("Notification", "MINUTE: $minute")
        // Schedule the alarm (one-time alarm)
        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Channel"
            val descriptionText = "Channel for my app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    companion object {
        const val CHANNEL_ID = "my_channel"
        const val ALARM_REQUEST_CODE = 123 // Use a unique request code
        const val PERMISSION_REQUEST_CODE = 456 // Use a unique request code for permissions
    }

    class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showNotification(context)
        }

        private fun showNotification(context: Context?) {
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create an intent for the notification
            val notificationIntent = Intent(context, NotificationActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                ALARM_REQUEST_CODE, // Use the same request code as in scheduleNotifications
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Build the notification
            val builder: Notification.Builder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder(context, CHANNEL_ID)
                        .setContentTitle("Notification Title")
                        .setContentText("This is a sample notification.")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                } else {
                    Notification.Builder(context) // For versions lower than Oreo
                        .setContentTitle("Notification Title")
                        .setContentText("This is a sample notification.")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                }

            // Notify
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}

