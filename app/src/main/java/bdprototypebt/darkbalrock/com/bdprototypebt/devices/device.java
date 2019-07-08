package bdprototypebt.darkbalrock.com.bdprototypebt.devices;

/*
* Clase device
* Define los atributos del artefacto dispositivo (device)
* */
public class device {

    private int id;                  //Consecutivo de la BD
    private String name;             //Nombre del dispositivo
    private String address;          //Dirección MAC del dispositivo
    private String UUIDs;            //Utilizado como un campo adicional en los intentos de ACTION_UUID, contiene los ParcelUuids del dispositivo remoto, que es una versión parcelable de UUID.
    private String contentDesc;      //Descripción de los servicios
    private String time;             //Momento de la conexion
    private String bonded;           //Estado de la conexión
    private String hashCode;         //Hash generado para el dispositivo
    private String bloqueado;        //Bandera para bloquear las comunicaciones

    /*Constructor*/
    public device() {
    }

    /*Getter y Setter*/
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

    public String getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(String bloqueado) {
        this.bloqueado = bloqueado;
    }
}
