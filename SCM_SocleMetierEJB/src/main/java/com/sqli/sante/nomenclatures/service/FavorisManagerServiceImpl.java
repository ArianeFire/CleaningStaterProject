package com.sqli.sante.nomenclatures.service;

import java.util.Collection;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import com.sqli.sante.common.exception.TechniqueException;
import com.sqli.sante.common.exception.UtilisationException;
import com.sqli.sante.nomenclature.service.bo.FavorisManagerServiceBO;
import com.sqli.sante.nomenclature2.dto.elementclassifiable.ElementClassifiableIdDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.ElementFavorisRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FavorisUserRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FavorisUserResponseDTO;

@SuppressWarnings("serial")
@Stateless
@Remote(FavorisManagerService.class)
public class FavorisManagerServiceImpl implements FavorisManagerService {

	
	private static final Logger log = Logger.getLogger(FavorisManagerServiceImpl.class);

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public FavorisUserResponseDTO getUserFavoris(FavorisUserRequestDTO pvFavorisUserRequestDTO)
			throws UtilisationException, TechniqueException {
		
		log.info("FavorisManagerService EJB Call : getUserFavoris(...)");
		
		return FavorisManagerServiceBO.getUserFavoris(pvFavorisUserRequestDTO);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Collection<ElementClassifiableIdDTO> saveElementsFavoris(ElementFavorisRequestDTO pvRequestDTO)
			throws UtilisationException, TechniqueException {
		
		log.info("FavorisManagerService EJB Call : saveElementsFavoris(...)");
		
		return FavorisManagerServiceBO.saveFavorisElements(pvRequestDTO);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Collection<ElementClassifiableIdDTO> deleteElementsFavoris(ElementFavorisRequestDTO pvRequestDTO) throws UtilisationException, TechniqueException {
		
		log.info("FavorisManagerService EJB Call : deleteElementsFavoris(...)");
		
		return FavorisManagerServiceBO.deleteFavorisElements(pvRequestDTO);
	}
}
