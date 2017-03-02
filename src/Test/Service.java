package Test;

public class Service {

	private String serviceId;
	private double pricePerGB ;
	private double pricePer10kGet ;
	private double priceOutbandwidth ;
	private double availibility ;
	private double delta;
	
	public Service(String serviceId, double pricePerGB, double pricePer10kGet, double priceOutbandwidth,
			double availibility, double delta) {
		super();
		this.serviceId = serviceId;
		this.pricePerGB = pricePerGB;
		this.pricePer10kGet = pricePer10kGet;
		this.priceOutbandwidth = priceOutbandwidth;
		this.availibility = availibility;
		this.delta = delta;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public double getPricePerGB() {
		return pricePerGB;
	}
	public void setPricePerGB(double pricePerGB) {
		this.pricePerGB = pricePerGB;
	}
	public double getPricePer10kGet() {
		return pricePer10kGet;
	}
	public void setPricePer10kGet(double pricePer10kGet) {
		this.pricePer10kGet = pricePer10kGet;
	}
	public double getPriceOutbandwidth() {
		return priceOutbandwidth;
	}
	public void setPriceOutbandwidth(double priceOutbandwidth) {
		this.priceOutbandwidth = priceOutbandwidth;
	}
	public double getAvailibility() {
		return availibility;
	}
	public void setAvailibility(double availibility) {
		this.availibility = availibility;
	}
	public double getDelta() {
		return delta;
	}
	public void setDelta(double delta) {
		this.delta = delta;
	}
	
}
