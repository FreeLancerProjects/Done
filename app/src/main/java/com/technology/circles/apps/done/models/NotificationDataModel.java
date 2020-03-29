package com.technology.circles.apps.done.models;

import java.io.Serializable;
import java.util.List;

public class NotificationDataModel implements Serializable {

    private Meta meta;
    private List<NotificationModel> data;

    public Meta getMeta() {
        return meta;
    }

    public List<NotificationModel> getData() {
        return data;
    }

    public static class Meta {

        private int current_page;
        private int last_page;

        public int getCurrent_page() {
            return current_page;
        }

        public int getLast_page() {
            return last_page;
        }
    }

    public class NotificationModel implements Serializable
    {
        private String notification_id;
        private String process_id_fk;
        private String process_type;
        private String from_user_id;
        private String to_user_id;
        private String action_type;
        private String notification_msg_id;
        private String notification_msg_other;
        private String from_to_type;
        private String created_at;
        private Word word;
        private UserModel from_user;
        private UserModel to_user;

        public String getNotification_id() {
            return notification_id;
        }

        public String getProcess_id_fk() {
            return process_id_fk;
        }

        public String getProcess_type() {
            return process_type;
        }

        public String getFrom_user_id() {
            return from_user_id;
        }

        public String getTo_user_id() {
            return to_user_id;
        }

        public String getAction_type() {
            return action_type;
        }

        public String getNotification_msg_id() {
            return notification_msg_id;
        }

        public String getNotification_msg_other() {
            return notification_msg_other;
        }

        public String getFrom_to_type() {
            return from_to_type;
        }

        public String getCreated_at() {
            return created_at;
        }

        public Word getWord() {
            return word;
        }

        public UserModel getFrom_user() {
            return from_user;
        }

        public UserModel getTo_user() {
            return to_user;
        }

        public class Word implements Serializable
        {
            private String id;
            private String msg_type;
            private String title;
            private String content;

            public String getId() {
                return id;
            }

            public String getMsg_type() {
                return msg_type;
            }

            public String getTitle() {
                return title;
            }

            public String getContent() {
                return content;
            }
        }

    }
}
