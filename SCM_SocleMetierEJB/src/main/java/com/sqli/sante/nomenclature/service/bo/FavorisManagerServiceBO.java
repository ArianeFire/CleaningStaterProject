package com.sqli.sante.nomenclature.service.bo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.sqli.sante.common.exception.TechniqueException;
import com.sqli.sante.common.exception.UtilisationException;
import com.sqli.sante.nomenclature.applicationcliente.mesfavoris.model.MesFavoris;
import com.sqli.sante.nomenclature.favori.FavoriUserBO;
import com.sqli.sante.nomenclature2.dto.elementclassifiable.ElementClassifiableIdDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.ElementFavorisRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FavorisUserRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FavorisUserResponseDTO;

public class FavorisManagerServiceBO {
	
	/**
	 * 
	 * Save Element as Given User's Favoris Element (Return not saved Elements => already exist)
	 * 
	 * @param pvFavorisElements
	 * @param pvUserId
	 * @param pvNomenclature
	 * @param pvProfilUtilisateur
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Collection<ElementClassifiableIdDTO> saveFavorisElements(ElementFavorisRequestDTO pvRequestDTO) throws TechniqueException, UtilisationException{
		
		Set<String> lvIgnoredCodes = FavoriUserBO.saveEltsFromUser(pvRequestDTO);
		
		List<ElementClassifiableIdDTO> lvECIdDTOs = lvIgnoredCodes.stream().map(value -> new ElementClassifiableIdDTO(value, null)).collect(Collectors.toList());
		
		return (lvECIdDTOs != null && !lvECIdDTOs.isEmpty() ? lvECIdDTOs : new ArrayList<ElementClassifiableIdDTO>());
	}
	
	/**
	 * 
	 * Delete Elements From Given User's Favoris (Return Elements When Execpetion occurs )
	 * 
	 * @param pvElementIds
	 * @param pvUserId
	 * @param pvNomenclature
	 * @param pvProfilUtilisateur
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Collection<ElementClassifiableIdDTO> deleteFavorisElements(ElementFavorisRequestDTO pvRequestDTO) throws TechniqueException, UtilisationException{
	
		Set<String> lvIgnoreElements = FavoriUserBO.deleteFavorisElements(pvRequestDTO);
		
		List<ElementClassifiableIdDTO> lvECIdDTOs = lvIgnoreElements.stream().map(value -> new ElementClassifiableIdDTO(value, null)).collect(Collectors.toList());
		
		return (lvECIdDTOs != null && !lvECIdDTOs.isEmpty() ? lvECIdDTOs : new ArrayList<>());
	}
	
	
	public static FavorisUserResponseDTO getUserFavoris(FavorisUserRequestDTO pvRequestDTO) throws TechniqueException, UtilisationException{

		/** Get User's Root Favoris **/
		Optional<Collection<MesFavoris>> lvMesFavoris = FavoriUserBO.getUserFavoris(pvRequestDTO, Optional.of(value -> StringUtils.equals(value.getPath(), "/")));
		
		return FavoriUserBO.getUserFavorisDTO(lvMesFavoris.isPresent() ? lvMesFavoris.get() : new ArrayList<MesFavoris>(), 
												pvRequestDTO.getTypeNomenclature()
											  ).get();
	}
}
