package common;

import java.io.Serializable;
import java.util.List;

public class MyMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String urlHtml;
	private String pathHtml;
	private List<String> urlImage;
	private List<String> pathImage;
	
	public MyMessage(String urlHtml) {
		super();
		this.urlHtml = urlHtml;
	}

	public MyMessage(String urlHtml, String pathHtml) {
		super();
		this.urlHtml = urlHtml;
		this.pathHtml = pathHtml;
	}

	public MyMessage(String urlHtml, String pathHtml, List<String> urlImage) {
		super();
		this.urlHtml = urlHtml;
		this.pathHtml = pathHtml;
		this.urlImage = urlImage;
	}

	public MyMessage(String urlHtml, String pathHtml, List<String> urlImage,
			List<String> pathImage) {
		super();
		this.urlHtml = urlHtml;
		this.pathHtml = pathHtml;
		this.urlImage = urlImage;
		this.pathImage = pathImage;
	}

	public String getUrlHtml() {
		return urlHtml;
	}

	public String getPathHtml() {
		return pathHtml;
	}

	public List<String> getUrlImage() {
		return urlImage;
	}

	public List<String> getPathImage() {
		return pathImage;
	}

	@Override
	public String toString() {
		return "MyMessage [urlHtml=" + urlHtml + ", pathHtml=" + pathHtml
				+ ", urlImage=" + urlImage + ", pathImage=" + pathImage + "]";
	}
	
}
