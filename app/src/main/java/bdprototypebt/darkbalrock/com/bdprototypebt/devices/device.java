package bdprototypebt.darkbalrock.com.bdprototypebt.devices;

public class device {

    private int id;
    private String name;
    private String address;
    private String UUIDs;
    private String contentDesc;
    private String time;
    private String bonded;
    private String hashCode;

    public device() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUUIDs() {
        return UUIDs;
    }

    public void setUUIDs(String UUIDs) {
        this.UUIDs = UUIDs;
    }

    public String getContentDesc() {
        return contentDesc;
    }

    public void setContentDesc(String contentDesc) {
        this.contentDesc = contentDesc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBonded() {
        return bonded;
    }

    public void setBonded(String bonded) {
        this.bonded = bonded;
    }

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }
}
