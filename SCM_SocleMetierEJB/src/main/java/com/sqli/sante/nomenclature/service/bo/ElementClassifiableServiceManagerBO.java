package com.sqli.sante.nomenclature.service.bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sqli.sante.common.exception.TechniqueException;
import com.sqli.sante.common.exception.UtilisationException;
import com.sqli.sante.common.security.ProfilUtilisateur;
import com.sqli.sante.nomenclature.UtilisateursGroupesFavoris.business.UtilisateursGroupesFavorisBo;
import com.sqli.sante.nomenclature.UtilisateursGroupesFavoris.model.UtilisateursGroupesFavoris;
import com.sqli.sante.nomenclature.applicationcliente.assfavorisentity.business.AssFavorisEntityBO;
import com.sqli.sante.nomenclature.applicationcliente.assfavorisentity.model.AssFavorisEntity;
import com.sqli.sante.nomenclature.applicationcliente.assfavorisfiltre.business.AssFavorisFiltreBO;
import com.sqli.sante.nomenclature.applicationcliente.elementfavoris.model.ElementFavoris;
import com.sqli.sante.nomenclature.applicationcliente.favorisentitefiltre.model.FavorisEntiteFiltre;
import com.sqli.sante.nomenclature.applicationcliente.mesfavoris.business.MesFavorisBo;
import com.sqli.sante.nomenclature.applicationcliente.synonyme.business.SynonymeBo;
import com.sqli.sante.nomenclature.applicationcliente.synonyme.model.Synonyme;
import com.sqli.sante.nomenclature.common.util.NomenclatureUtil.NomenclatureAppliquable;
import com.sqli.sante.nomenclature.elementclassifiable.business.impl.GererElementClassifiableBO;
import com.sqli.sante.nomenclature.elementclassifiable.model.ElementClassifiable;
import com.sqli.sante.nomenclature.elementclassifiable.relation.business.RelationBO;
import com.sqli.sante.nomenclature.elementclassifiable.relation.mapper.RelationMapper;
import com.sqli.sante.nomenclature.elementclassifiable.service.impl.ElementClassifiableServiceImpl;
import com.sqli.sante.nomenclature.enums.UserTypeEnum;
import com.sqli.sante.nomenclature.favori.FavoriUserBO;
import com.sqli.sante.nomenclature.mapper.ElementClassifiableMapper;
import com.sqli.sante.nomenclature.protocole.business.ProtocoleBO;
import com.sqli.sante.nomenclature.protocole.model.Protocole;
import com.sqli.sante.nomenclature.referentiel.nomenclature.model.Nomenclature;
import com.sqli.sante.nomenclature.referentiel.service.impl.TypeRelationServiceImpl;
import com.sqli.sante.nomenclature.referentiel.typerelation.model.TypeRelation;
import com.sqli.sante.nomenclature.relation.model.Relation;
import com.sqli.sante.nomenclature2.dto.ComposantNomenclatureRequestDTO;
import com.sqli.sante.nomenclature2.dto.elementclassifiable.ElementClassifiableReponseDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.ECRelationDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.ElementClassifiableRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.NomenclatureRequestDTO;
import com.sqli.sante.nomenclatures.dto.entity.EntityIdDTO;

public class ElementClassifiableServiceManagerBO {

	private static final Logger log = Logger.getLogger(ElementClassifiableServiceManagerBO.class);
	
	/**
	 * 
	 * Get {@link AssFavorisEntity}s associated to the CONTEXT_UTILISATION passed to the request
	 * 
	 * @param request
	 * @param nomenclature
	 * @param nnContexteUtilisation
	 * @param typeNomenclature
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Optional<Collection<AssFavorisEntity>> getAssFavorisEntityByContext(NomenclatureRequestDTO request, Nomenclature nomenclature, String nnContexteUtilisation)
			throws TechniqueException, UtilisationException {
		
		Optional<Collection<AssFavorisEntity>> lvAFEQueryResult = getAssFavEntite(request, nomenclature.getType());
		Set<Long> lvAFEFavoris = lvAFEQueryResult.isPresent() ? lvAFEQueryResult.get().stream().map(value -> value.getFavorisId()).collect(Collectors.toSet()):new HashSet<>();
				
		/** FILTER RETURNED FAVORIS BASED ON CONTEXT_UTILISATION **/
		if(StringUtils.isNotBlank(nnContexteUtilisation)){
				
			Collection<FavorisEntiteFiltre> lvFavEntFiltres = AssFavorisFiltreBO.getAssFavorisFiltreService().findByFavorisIds(nomenclature.getType(), nomenclature.getNomenclatureId() , lvAFEFavoris, Boolean.TRUE, null);
			lvAFEFavoris = lvFavEntFiltres.stream().filter(value -> value.getCriteres().contains(nnContexteUtilisation)).map(FavorisEntiteFiltre::getFavorisId).collect(Collectors.toSet());
		}
				
		/** GET AssFavorisEntity IN THE SPECIFIED CONTEXT **/
		Set<Long> lvAFEFavorisV2 = (lvAFEFavoris != null && !lvAFEFavoris.isEmpty()) ? lvAFEFavoris : new HashSet<Long>(); 
		Collection<AssFavorisEntity> lvAssEntFav =  (lvAFEQueryResult.isPresent() ? lvAFEQueryResult.get() : new ArrayList<AssFavorisEntity>()).stream().filter(value -> !lvAFEFavorisV2.isEmpty() && lvAFEFavorisV2.contains(value.getFavorisId())).collect(Collectors.toList());
		
		return (lvAssEntFav != null && !lvAssEntFav.isEmpty()) ? Optional.of(lvAssEntFav) : Optional.empty();
	}

	/**
	 * @param request
	 * @param typeNomenclature
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Optional<Collection<AssFavorisEntity>> getAssFavEntite(NomenclatureRequestDTO request,
			String typeNomenclature) throws TechniqueException, UtilisationException {
		AssFavorisEntity lvAssFavEntRequestDTO = new AssFavorisEntity();
				
		if((request.getIdEtablissement() != null || request.getIdUniteFonctionnelle() != null) /** && (listeEcComplete!=null && !listeEcComplete.isEmpty()) **/){
					
			/** FILTRE FAVORIS ETABILISSEMENT**/
			if(request.getIdEtablissement() != null){
				lvAssFavEntRequestDTO.setIdeomedEntityId(request.getIdEtablissement().toString());
			}
					
			/** FILTRE FAVORIS UNITE FONCTIONNELLE **/
			if(request.getIdUniteFonctionnelle() != null){
				lvAssFavEntRequestDTO.setIdeomedUfId(request.getIdUniteFonctionnelle().toString());
			}
					
		}else /*if((listeEcComplete!=null && !listeEcComplete.isEmpty()))*/{ /** TAG FAVORIS COMMUNS (ETAB + UFS)**/
					
			lvAssFavEntRequestDTO.setAllEtabs(Boolean.TRUE);
			lvAssFavEntRequestDTO.setUfs(Boolean.TRUE);
		}
				
		/** RETRIEVE ECs FAVORIS PUBLIC **************************************/
		Optional<Collection<AssFavorisEntity>> lvAFEQueryResult =  AssFavorisEntityBO.getAssFavorisEntityService().find(typeNomenclature, lvAssFavEntRequestDTO, Optional.of(value -> value.getDateDesactivation() == null), Optional.of("OR"), null);
		return lvAFEQueryResult;
	}

	/**
	 * 
	 * Recherche {@link ElementClassifiableReponseDTO} by S
	 * 
	 * @param lVersionId
	 * @param listeSynonymes
	 * @param typeNomenclature
	 * @param listeEcComplete
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Optional<Collection<ElementClassifiableReponseDTO>> searchECsBySynonymes(Long lVersionId, Collection<Synonyme> listeSynonymes, String typeNomenclature, Collection<ElementClassifiableReponseDTO> listeEcComplete) throws TechniqueException, UtilisationException {
		
		Set<Long> lvResultECIds = new HashSet<Long>();
		if(listeEcComplete != null && !listeEcComplete.isEmpty()){
			lvResultECIds.addAll(listeEcComplete.stream().map(value -> value.getElementClassifiableId().getIdEc()).collect(Collectors.toSet()));
		}
				
		Set<Long> lvSynonymeECIds = listeSynonymes.stream()
														.map(value -> value.getConceptId())
														.collect(Collectors.toSet())
														.stream()
														.filter(value -> !lvResultECIds.contains(value))
														.collect(Collectors.toSet());
				
		Collection<ElementClassifiable> lvECs = new ElementClassifiableServiceImpl().getConceptsByIds(lvSynonymeECIds, typeNomenclature, Arrays.asList(lVersionId), null);
		
		return (lvECs != null && !lvECs.isEmpty()) ? Optional.of(ElementClassifiableMapper.createElementClasfiableReponseDTOs(lvECs)) : Optional.empty();
	}

	/**
	 * 
	 * Met à jour les pseudos de la Liste d'ECs en se basant sur les {@link Synonyme} les Synonnymes
	 * 
	 * @param mapEcComplete
	 * @param mapIdSynonyme
	 * @param listeEcComplete
	 */
	public static Collection<ElementClassifiableReponseDTO> getOrUpdateECsBySynonyme(Map<Long, ElementClassifiableReponseDTO> mapEcComplete,
			Map<Long, Synonyme> mapIdSynonyme) {
		
		Collection<ElementClassifiableReponseDTO> listeEcComplete = new ArrayList<>();
		
		// boucle sur .values() sinon : Caused by: org.jboss.serial.exception.SerializationException: Could not
		// create instance of java.util.HashMap$Values
		mapEcComplete.values().forEach(value -> listeEcComplete.add(getOrUpdateECBySynonyme(mapIdSynonyme, value)));
		
		return listeEcComplete;
	}

	/**
	 * 
	 * Met à jour (le champs libelle) de {@link ElementClassifiableReponseDTO} si l'EC a un {@link Synonyme} parmi le Map "EC ID" => Synonyme
	 * 
	 * Sinon retourne {@link ElementClassifiableReponseDTO}
	 * 
	 * @param mapIdSynonyme
	 * @param listeEcComplete
	 * @param ec
	 */
	public static ElementClassifiableReponseDTO getOrUpdateECBySynonyme(Map<Long, Synonyme> mapIdSynonyme, ElementClassifiableReponseDTO ec) {
		Synonyme synonyme = mapIdSynonyme.get(ec.getElementClassifiableId().getIdEc());
		if (synonyme != null) {
			// si élément est synonyme, on augmente son poids pour le remonter dans la liste affichée
			ec.setFavoriPoidsPrecision(ec.getFavoriPoidsPrecision() != null ? ec.getFavoriPoidsPrecision() + 1 : 1);

			// on change le libellé : "LIBELLE_SYNONYME (NOM COMPLET DE L'ELEMENT)"
			String libelleSynonyme = synonyme.getLibelleSynonyme() + " (" + ec.getPseudoLibelle() + ")";
			ec.setPseudoLibelle(libelleSynonyme);
		}
		
		return ec;
	}

	/**
	 * 
	 * Recherche de {@link Synonyme} par leurs libelles +  Mapping {@link Synonyme} Id => {@link Synonyme}
	 * 
	 * @param libelleRecherche
	 * @param mapIdSynonyme
	 * @param n
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Collection<Synonyme> getSynonymesByLibelle(String libelleRecherche, Map<Long, Synonyme> mapIdSynonyme,
			NomenclatureAppliquable n, ElementClassifiableRequestDTO pvECRequestDTO) throws TechniqueException, UtilisationException {
		
		Synonyme rechercheSynonyme = new Synonyme();
		rechercheSynonyme.setLibelleSynonyme(libelleRecherche);
		Collection<Synonyme> listeSynonymes = SynonymeBo.getByFormattedLibelle(libelleRecherche, n, Boolean.TRUE, Optional.of(pvECRequestDTO));
				//SynonymeBo.listerSynonymeActifInactif(rechercheSynonyme, n.getNomenclatureCode(), n.getNomenclatureId(), Boolean.TRUE);
		
		listeSynonymes.forEach(value -> mapIdSynonyme.put(value.getConceptId(), value));
		
		return listeSynonymes;
	}

	/**
	 * 
	 * Recherche par defaut en utilisant du Text
	 * 
	 * @param requestDTO
	 * @param request
	 * @param mapEcComplete
	 * @param mapEcFiltre
	 * @param lRequest
	 * @param nomenclature
	 * @param rechercheOnlyFavoris
	 * @param rechercheOnlyListes
	 * @param returnExceptionIfTooManyResult
	 * @param isSousListe
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static void defaultSearch(ComposantNomenclatureRequestDTO requestDTO, NomenclatureRequestDTO request,
			Map<Long, ElementClassifiableReponseDTO> mapEcComplete,
			Map<Long, ElementClassifiableReponseDTO> mapEcFiltre, ElementClassifiableRequestDTO lRequest,
			Nomenclature nomenclature, boolean rechercheOnlyFavoris, boolean rechercheOnlyListes,
			boolean returnExceptionIfTooManyResult, boolean isSousListe)
			throws TechniqueException, UtilisationException {
		
		long t = System.currentTimeMillis();
		Collection<ElementClassifiableReponseDTO> elementsDtos = new ArrayList<ElementClassifiableReponseDTO>();
		elementsDtos = GererElementClassifiableBO.searchNomenclatures(nomenclature, lRequest, null,
				ElementClassifiableReponseDTO.class, (requestDTO == null ? new ArrayList<EntityIdDTO>()
						: request.getEntities()), returnExceptionIfTooManyResult);
		log.debug("-Nomenclature : recherche éléments en " + (System.currentTimeMillis() - t) + " ms");
		if (elementsDtos != null) {
			
			if (isSousListe) {
				
				// on doit garder que les éléménts de cette sous liste
				elementsDtos.forEach(value -> mapEcFiltre.put(value.getElementClassifiableId().getIdEc(), value));
			}
			
			if (!rechercheOnlyListes && !rechercheOnlyFavoris) {
				
				// on les garde tous
				elementsDtos.forEach(value -> mapEcComplete.put(value.getElementClassifiableId().getIdEc(), value));
			}
		}
	}

	/**
	 * 
	 * Tag les favoris public
	 * 
	 * @param lVersionId
	 * @param typeNomenclature
	 * @param listeEcComplete
	 * @param lvAFEQueryResult
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Collection<ElementClassifiableReponseDTO> tagPublicFavorisECs(Long lVersionId, String typeNomenclature,
			Collection<ElementClassifiableReponseDTO> listeEcComplete,
			Optional<Collection<AssFavorisEntity>> lvAFEQueryResult) throws TechniqueException, UtilisationException {
		
		Set<Long> lvAFEFavoris = lvAFEQueryResult.get().stream().map(value -> value.getFavorisId()).collect(Collectors.toSet());
		Optional<Collection<ElementClassifiable>> lvAFEECs = FavoriUserBO.getECsFromFavorisIds(typeNomenclature, lVersionId, lvAFEFavoris); //MesFavorisBo.getElementClassifiableFromMesFavoris(typeNomenclature, lVersionId, lvAFEFavoris);

		if(lvAFEECs.isPresent()){
			Set<Long> lvAFEECIDs = lvAFEECs.get().stream().map(value -> value.getElementClassifiableId()).collect(Collectors.toSet());
			listeEcComplete = listeEcComplete.stream().map(value -> {
				
				ElementClassifiableReponseDTO lvTmpEC = value;
				
				if(lvAFEECIDs.contains(value.getElementClassifiableId().getIdEc())){
					lvTmpEC.setIsFavoriPublic(Boolean.TRUE);
				}
				
				return lvTmpEC;
			}).collect(Collectors.toList());
		}
		
		return listeEcComplete;
	}
	
	
	/**
	 * 
	 * Tag les favoris public
	 * 
	 * @param lVersionId
	 * @param typeNomenclature
	 * @param listeEcComplete
	 * @param lvAFEQueryResult
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Collection<ElementClassifiableReponseDTO> tagPublicFavorisECs(Long lVersionId, String typeNomenclature,
			Collection<ElementClassifiableReponseDTO> listeEcComplete,
			Collection<ElementClassifiable> lvAFEECs) throws TechniqueException, UtilisationException {
		
		//Set<Long> lvAFEFavoris = lvAFEQueryResult.get().stream().map(value -> value.getFavorisId()).collect(Collectors.toSet());
		//Collection<ElementClassifiable> lvAFEECs = //FavoriUserBO.getECsFromFavorisIds(typeNomenclature, lVersionId, lvAFEFavoris); //MesFavorisBo.getElementClassifiableFromMesFavoris(typeNomenclature, lVersionId, lvAFEFavoris);

		Set<Long> lvAFEECIDs = lvAFEECs.stream().map(value -> value.getElementClassifiableId()).collect(Collectors.toSet());
		listeEcComplete = listeEcComplete.stream().map(value -> {
				
			ElementClassifiableReponseDTO lvTmpEC = value;
				
			if(lvAFEECIDs.contains(value.getElementClassifiableId().getIdEc())){
				lvTmpEC.setIsFavoriPublic(Boolean.TRUE);
			}
			
			
				
			return lvTmpEC;
		}).collect(Collectors.toList());
		
		return listeEcComplete;
	}
	
	/**
	 * 
	 * Tag les favoris public
	 * 
	 * @param lVersionId
	 * @param typeNomenclature
	 * @param listeEcComplete
	 * @param lvAFEQueryResult
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Collection<ElementClassifiableReponseDTO> tagPublicFavorisECsWithDTO(Long lVersionId, String typeNomenclature,
			Collection<ElementClassifiableReponseDTO> listeEcComplete,
			Collection<ElementClassifiableReponseDTO> lvAFEECs) throws TechniqueException, UtilisationException {
		
		//Set<Long> lvAFEFavoris = lvAFEQueryResult.get().stream().map(value -> value.getFavorisId()).collect(Collectors.toSet());
		//Collection<ElementClassifiable> lvAFEECs = //FavoriUserBO.getECsFromFavorisIds(typeNomenclature, lVersionId, lvAFEFavoris); //MesFavorisBo.getElementClassifiableFromMesFavoris(typeNomenclature, lVersionId, lvAFEFavoris);

		Set<Long> lvAFEECIDs = lvAFEECs.stream().map(value -> value.getElementClassifiableId().getIdEc()).collect(Collectors.toSet());
		listeEcComplete = listeEcComplete.stream().map(value -> {
				
			ElementClassifiableReponseDTO lvTmpEC = value;
				
			if(lvAFEECIDs.contains(value.getElementClassifiableId().getIdEc())){
				lvTmpEC.setIsFavoriPublic(Boolean.TRUE);
			}
				
			return lvTmpEC;
		}).collect(Collectors.toList());
		
		return listeEcComplete;
	}
	
	/**
	 * 
	 * Get Type of {@link Nomenclature} From {@link ComposantNomenclatureRequestDTO}
	 * 
	 * @param requestDTO
	 * @param typeNomenclature
	 * @return
	 */
	public static String getTypeNomenclatureFromRequestDTO(ComposantNomenclatureRequestDTO requestDTO) {
		
		String typeNomenclature = null;
		
		if (requestDTO.getNomenclature().getCode() != null) {
			typeNomenclature = requestDTO.getNomenclature().getCode().getTitle();
		} else if (StringUtils.isNotBlank(requestDTO.getNomenclature().getCodeLibre())) {
			typeNomenclature = requestDTO.getNomenclature().getCodeLibre();
		}
		
		return typeNomenclature;
	}

	/**
	 * 
	 * Search Protocole Based on 
	 * 
	 * @param pvRequestDTO
	 * @param pvActif
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Optional<Collection<Protocole>> searchProtocoles(ElementClassifiableRequestDTO pvRequestDTO, Boolean pvActif) throws TechniqueException, UtilisationException{
		
		return ProtocoleBO.getByLibelleOrElementCode(Optional.of(pvRequestDTO), pvActif);
	}
	
	/**
	 * 
	 * Delete FavorisElements
	 * 
	 * @param pvFavorisId
	 * @param pvFavorisElementIds
	 * @param pvNomenclature
	 * @param pvProfilUtilisateur
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static void deleteFavoisElements(Long pvFavorisId, Set<Long> pvFavorisElementIds, String pvNomenclature, ProfilUtilisateur pvProfilUtilisateur) throws TechniqueException, UtilisationException{
		
		MesFavorisBo.deleteFavorisElements(pvFavorisId, pvFavorisElementIds, pvNomenclature, pvProfilUtilisateur);
	}
	
	/**
	 * 
	 * Associate {@link ECRelationDTO}s  to Each {@link ElementClassifiableReponseDTO}
	 * 
	 * @param pvNomenclature
	 * @param pvTypeRelations
	 * @param pvECs
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Collection<ElementClassifiableReponseDTO> associateRelations(String pvNomenclature, Optional<List<String>> pvTypeRelations, Collection<ElementClassifiableReponseDTO> pvECs) throws TechniqueException, UtilisationException{
		
		Set<Long> lvElementIds = pvECs.stream().map(value -> value.getElementClassifiableId().getIdEc()).collect(Collectors.toSet());
		
		/** GET TypeRelationIds For Filter Purpose From TypeRelation codes **/
		Optional<Collection<TypeRelation>> lvOptTypeRelation = pvTypeRelations.isPresent() 
																				? new TypeRelationServiceImpl().findByCodes(pvTypeRelations.get().stream().collect(Collectors.toSet())) 
																				: Optional.empty();
		Set<Long> lvTypeRelationsIds = lvOptTypeRelation.isPresent() 
																? lvOptTypeRelation.get().stream().map(TypeRelation::getTypeRelationId).collect(Collectors.toSet())
																: new HashSet<Long>();
									
																				
		Optional<Map<Long, List<Relation>>> lvMappedECIdRelations = RelationBO.getMapBySourceOrTargetIds(
																				pvNomenclature, 
																				lvElementIds, 
																				
																				/** FILTER EACH RETURNED "EC" BASED ON TYPE OF RELATIONS SPECIFIED IN PARAMTER "pvTypeRelations" **/
																				pvTypeRelations.isPresent() ? Optional.of(eachReturnedRelation ->  lvTypeRelationsIds.contains(eachReturnedRelation.getTypeRelationId()))
																											: Optional.empty(),
																				Boolean.TRUE);
		
		if(lvMappedECIdRelations.isPresent() && lvMappedECIdRelations.get().size() > 0){
			
			pvECs = pvECs.stream().map(value -> {
				
				try {
					value.setRelations(RelationMapper.createRelationDTOs(lvMappedECIdRelations.get().get(value.getElementClassifiableId().getIdEc()), pvNomenclature).get());
				} catch (TechniqueException | UtilisationException e) {
					e.printStackTrace();
				}
				
				return value;
			}).collect(Collectors.toList());
		}
		
		return pvECs;
	}
	
	/**
	 * 
	 * Set Groupe Ids For Each Given {@link ElementClassifiableReponseDTO}
	 * 
	 * @param pvNomenclature
	 * @param pvECs
	 * @return
	 * @throws TechniqueException
	 * @throws UtilisationException
	 */
	public static Collection<ElementClassifiableReponseDTO> associateGroupes(String pvNomenclature, Collection<ElementClassifiableReponseDTO> pvECs) throws TechniqueException, UtilisationException{
		
		/** Create Map Between "ElementClassifiable Code" to "FavorisIds" **/
		Set<String> lvCodes = pvECs.stream().map(ElementClassifiableReponseDTO::getCode).collect(Collectors.toSet());
		Optional<Collection<ElementFavoris>> lvOptECFavoris = FavoriUserBO.getElementFavorisService().findByCodesAndFavorisId(null, lvCodes, pvNomenclature, Boolean.TRUE);
		if(!lvOptECFavoris.isPresent()){
			return pvECs;
		}
		
		Map<String, Set<Long>> lvMappedECCodeToFavorisId =  lvOptECFavoris.get().stream()
																			.collect(
																					Collectors.groupingBy(ElementFavoris::getCode, 
																					HashMap::new, 
																					Collectors.mapping(ElementFavoris::getFavorisId, Collectors.toSet()
																				)));
		
		/** Create Map Between "Favoris Ids" to "Groupe Ids"  **/
		Set<Long> pvFavIds = lvOptECFavoris.get().stream().map(ElementFavoris::getFavorisId).collect(Collectors.toSet());
		Optional<Collection<UtilisateursGroupesFavoris>> lvOptUserGroupeFavoris = FavoriUserBO.getFavorisUserGroupeService().findByFavorisIdsAndTypes(pvFavIds, Arrays.asList("G"), Optional.empty(), null);
		if(!lvOptUserGroupeFavoris.isPresent()){
			return pvECs;
		}
		
		Map<Long, Set<Long>> lvMappedFavorisIdToGroupeIds = lvOptUserGroupeFavoris.get().stream()
							.collect(
									Collectors.groupingBy(
											UtilisateursGroupesFavoris::getFavorisId, 
											HashMap::new, 
											Collectors.mapping(UtilisateursGroupesFavoris::getUsergroupId, Collectors.toSet()
									)));
		
		/** Set Groupe Ids for each Ec ElementClassifiableReponseDTO **/
		return pvECs.stream().map(value -> setECResponseGroupes(value, lvMappedECCodeToFavorisId, lvMappedFavorisIdToGroupeIds)).collect(Collectors.toList());
	}
	
	/**
	 * 
	 * Set Groupe Ids for Given {@link ElementClassifiableReponseDTO} 
	 * 
	 * @param pvEC
	 * @param pvMappedECCodeToFavorisId
	 * @param pvMappedFavorisIdToGroupeIds
	 * @return
	 */
	public static ElementClassifiableReponseDTO setECResponseGroupes(ElementClassifiableReponseDTO pvEC, Map<String, Set<Long>> pvMappedECCodeToFavorisId, Map<Long, Set<Long>> pvMappedFavorisIdToGroupeIds){
		
		pvEC.setGroupesId(new ArrayList<>());
		
		Set<Long> lvFavorisIds = pvMappedECCodeToFavorisId.get(pvEC.getCode());
		if(lvFavorisIds == null){
			return pvEC;
		}
		
		Set<Long> lvIds = new HashSet<>();
		
		lvFavorisIds.stream().forEach(value -> {
			
			Set<Long> lvGroupeIds = pvMappedFavorisIdToGroupeIds.get(value);
			if(lvGroupeIds != null && !lvGroupeIds.isEmpty()){
				lvIds.addAll(lvGroupeIds);
			}
			
		});
		
		pvEC.getGroupesId().addAll(lvIds);
		
		return pvEC;
	}
	
	public static Set<Long> getUserFavoriteECIds(Long pvUserId, UserTypeEnum pvUserType, String pvNomenclature, Long pvVersionId, ElementClassifiableRequestDTO pRequest) throws TechniqueException, UtilisationException{
		
		 Set<Long> lvUserFavorisECIds = new HashSet<>();

	        /** TAG FAVORIS USER + FAVORIS GROUPS ASSOCIATED TO THE USER **/
	        Optional<Collection<UtilisateursGroupesFavoris>> lvUserAndItGroupesFav = Optional
	                .of(new ArrayList<UtilisateursGroupesFavoris>());
	        
	        if (pvUserId != null) {
	        	
	        	if( UserTypeEnum.SIMPLE_USER.getUserType().equals(pvUserType.getUserType()) && pRequest.getIncludeUserGroupesFavoris()){
	        		
	        		lvUserAndItGroupesFav = UtilisateursGroupesFavorisBo.getUserAndItGroupeFavorisByUserId(
	                        pvNomenclature, pvUserId); 
	        	}else {
	        		
	        		lvUserAndItGroupesFav = UtilisateursGroupesFavorisBo.getByIdsAndTypes(
	        				new HashSet<>(Arrays.asList(pvUserId)), Arrays.asList(pvUserType.getUserType()), 
	        				pvNomenclature, null);
	        	}
	        }

	        /** GET (USER + IT GROUP)'S FAVORIS_ID **/
	        Set<Long> lvUserAndItGroupesFavIds = lvUserAndItGroupesFav.get().stream()
	                .map(UtilisateursGroupesFavoris::getFavorisId)
	                .collect(Collectors.toSet()); /* new HashSet<Long>(Arrays.asList(13L)); */
	        // Collection<MesFavoris> favoris = MesFavorisBo.getMesFavoris(pNomenclature.getNomenclatureCode(),
	        // pNomenclature.getNomenclature().getNomenclatureId(), lvUserAndItGroupesFavIds);
	        

	        /** GET ECs ASSOCIATED TO (USER + IT GROUP)'S FAVORIS **/
	        Optional<Collection<ElementClassifiable>> lvUserGroupFavEcs = FavoriUserBO
	                .getECsFromFavorisIds(pvNomenclature, pvVersionId, lvUserAndItGroupesFavIds);

	        if (lvUserGroupFavEcs.isPresent() && !lvUserGroupFavEcs.get().isEmpty()) {
	        	
	        	/** User's Favorite Element Classsifiable Ids  **/
	        	lvUserFavorisECIds = lvUserGroupFavEcs.get().stream().map(ElementClassifiable::getElementClassifiableId).collect(Collectors.toSet());
	        }
	
	        return lvUserFavorisECIds;
	}
}
