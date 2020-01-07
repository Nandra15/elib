package com.joatsy.apps.elibrarystta.Data;

public class ItemsItem{
	private int qty;
	private int idBuku;

	public ItemsItem(int qty, int idBuku){
		this.qty = qty;
		this.idBuku= idBuku;
	}

	public void setQty(int qty){
		this.qty = qty;
	}

	public int getQty(){
		return qty;
	}

	public void setIdBuku(int idBuku){
		this.idBuku = idBuku;
	}

	public int getIdBuku(){
		return idBuku;
	}

	@Override
 	public String toString(){
		return 
			"ItemsItem{" + 
			"qty = '" + qty + '\'' + 
			",id_buku = '" + idBuku + '\'' + 
			"}";
		}
}
