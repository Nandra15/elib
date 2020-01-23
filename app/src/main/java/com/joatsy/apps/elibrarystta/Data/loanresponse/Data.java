package com.joatsy.apps.elibrarystta.Data.loanresponse;

public class Data{
	private int id_peminjaman;

	public void setIdPeminjaman(int idPeminjaman){
		this.id_peminjaman = idPeminjaman;
	}

	public int getIdPeminjaman(){
		return id_peminjaman;
	}

	@Override
 	public String toString(){
		return 
			"Data{" + 
			"id_peminjaman = '" + id_peminjaman + '\'' +
			"}";
		}
}
