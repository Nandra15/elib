package com.joatsy.apps.elibrarystta.Data;

import java.util.List;

public class ResponseRequest{
	private int idMember;
	private String tanggal;
	private int durasi;
	private List<ItemsItem> items;

	public ResponseRequest(int id, String tanggal, int durasi, List<ItemsItem> items){
		setIdMember(id);
		setTanggal(tanggal);
		setDurasi(durasi);
		setItems(items);
	}

	public void setIdMember(int idMember){
		this.idMember = idMember;
	}

	public int getIdMember(){
		return idMember;
	}

	public void setTanggal(String tanggal){
		this.tanggal = tanggal;
	}

	public String getTanggal(){
		return tanggal;
	}

	public void setDurasi(int durasi){
		this.durasi = durasi;
	}

	public int getDurasi(){
		return durasi;
	}

	public void setItems(List<ItemsItem> items){
		this.items = items;
	}

	public List<ItemsItem> getItems(){
		return items;
	}

	@Override
 	public String toString(){
		return 
			"ResponseRequest{" + 
			"id_member = '" + idMember + '\'' + 
			",tanggal = '" + tanggal + '\'' + 
			",durasi = '" + durasi + '\'' + 
			",items = '" + items + '\'' + 
			"}";
		}
}