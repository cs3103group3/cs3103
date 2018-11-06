package main.tracker;

public class Tuple {
	
	public String ipAdd;
	public String portNo;
	
	public Tuple(String ip, String port) {
		this.ipAdd = ip;
		this.portNo = port;
	}
	
	public String getIpAdd() {
		return this.ipAdd;
	}
	
	public String getPortNo() {
		return this.portNo;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tuple other = (Tuple) obj;
        if (!this.ipAdd.equals(other.getIpAdd())) {
           return false;
        }
        if (!this.portNo.equals(other.getPortNo())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 53 * ipAdd.hashCode() + portNo.hashCode();
        return hash;
    }
}
