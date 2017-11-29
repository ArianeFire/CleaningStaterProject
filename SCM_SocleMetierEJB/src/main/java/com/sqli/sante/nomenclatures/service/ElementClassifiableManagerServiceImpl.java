package com.sqli.sante.nomenclatures.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sqli.sante.common.exception.TechniqueException;
import com.sqli.sante.common.exception.UtilisationException;
import com.sqli.sante.common.security.ProfilUtilisateur;
import com.sqli.sante.nomenclature.UtilisateursGroupesFavoris.business.UtilisateursGroupesFavorisBo;
import com.sqli.sante.nomenclature.UtilisateursGroupesFavoris.model.UtilisateursGroupesFavoris;
import com.sqli.sante.nomenclature.applicationcliente.assfavorisentity.model.AssFavorisEntity;
import com.sqli.sante.nomenclature.applicationcliente.asslisteentity.business.AssListeEntityBO;
import com.sqli.sante.nomenclature.applicationcliente.asslisteentity.model.AssListeEntity;
import com.sqli.sante.nomenclature.applicationcliente.liste.business.GestionListeBO;
import com.sqli.sante.nomenclature.applicationcliente.liste.model.Liste;
import com.sqli.sante.nomenclature.applicationcliente.mesfavoris.model.MesFavoris;
import com.sqli.sante.nomenclature.applicationcliente.synonyme.model.Synonyme;
import com.sqli.sante.nomenclature.common.util.GestionNomenclaturesConstantes;
import com.sqli.sante.nomenclature.common.util.NomenclatureUtil;
import com.sqli.sante.nomenclature.common.util.NomenclatureUtil.NomenclatureAppliquable;
import com.sqli.sante.nomenclature.elementclassifiable.business.impl.GererElementClassifiableBO;
import com.sqli.sante.nomenclature.elementclassifiable.frequenceutilisation.model.FrequenceUtilisation;
import com.sqli.sante.nomenclature.elementclassifiable.frequenceutilisation.service.impl.FrequnceUtilisationServiceImpl;
import com.sqli.sante.nomenclature.elementclassifiable.historiquepatient.model.HistoriquePatient;
import com.sqli.sante.nomenclature.elementclassifiable.historiquepatient.serviceimpl.HistoriquePatientServiceImpl;
import com.sqli.sante.nomenclature.elementclassifiable.model.ElementClassifiable;
import com.sqli.sante.nomenclature.elementclassifiable.service.IElementClassifiableService;
import com.sqli.sante.nomenclature.elementclassifiable.service.impl.ElementClassifiableServiceImpl;
import com.sqli.sante.nomenclature.enums.UserTypeEnum;
import com.sqli.sante.nomenclature.favori.FavoriUserBO;
import com.sqli.sante.nomenclature.mapper.ElementClassifiableMapper;
import com.sqli.sante.nomenclature.nomenclatureversion.bo.NomenclatureVersionBo;
import com.sqli.sante.nomenclature.protocole.model.Protocole;
import com.sqli.sante.nomenclature.raccourci.model.Raccourci;
import com.sqli.sante.nomenclature.raccourci.service.IRaccourciService;
import com.sqli.sante.nomenclature.raccourci.service.impl.RaccourciServiceImpl;
import com.sqli.sante.nomenclature.referentiel.business.NomenclatureBO;
import com.sqli.sante.nomenclature.referentiel.nomenclature.mapper.NomenclatureMapper;
import com.sqli.sante.nomenclature.referentiel.nomenclature.model.Nomenclature;
import com.sqli.sante.nomenclature.referentiel.nomenclature.nomenclatureversion.model.NomenclatureVersion;
import com.sqli.sante.nomenclature.service.bo.ElementClassifiableServiceManagerBO;
import com.sqli.sante.nomenclature.transcodage.business.impl.GererCodeExterneBO;
import com.sqli.sante.nomenclature.transcodage.codeexterne.model.CodeExterne;
import com.sqli.sante.nomenclature.transcodage.codeexterne.service.ICodeExterneService;
import com.sqli.sante.nomenclature.transcodage.codeexterne.service.impl.CodeExterneServiceImpl;
import com.sqli.sante.nomenclature.transcodage.systemecodage.model.SystemeCodage;
import com.sqli.sante.nomenclature.transcodage.systemecodage.service.ISystemeCodageService;
import com.sqli.sante.nomenclature.transcodage.systemecodage.service.impl.SystemeCodageServiceImpl;
import com.sqli.sante.nomenclature.transcodagenomenclature.business.TranscodageNomenclatureBO;
import com.sqli.sante.nomenclature2.dto.ComposantNomenclatureRequestDTO;
import com.sqli.sante.nomenclature2.dto.elementclassifiable.ElementClassifiableIdDTO;
import com.sqli.sante.nomenclature2.dto.elementclassifiable.ElementClassifiableReponseDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.ECMetadonneeDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.ElementClassifiableDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.ElementClassifiableRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.ElementClassifiableSearchParamsDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FavorisElementDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FavorisUserRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FavorisUserResponseDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FrequenceUtilisationDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.FrequenceUtilisationRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.HistoriquePatientDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.HistoriquePatientRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.NomenclatureRequestDTO;
import com.sqli.sante.nomenclatures.dto.elementclassifiable.NomenclatureResponseDTO;
import com.sqli.sante.nomenclatures.dto.liste.TitreCritereEnum;
import com.sqli.sante.nomenclatures.dto.nomenclature.NomenclatureDTO;
import com.sqli.sante.nomenclatures.dto.nomenclature.NomenclatureIdDTO;
import com.sqli.sante.nomenclatures.dto.nomenclature.TypeNomenclatureRequestDTO;
import com.sqli.sante.nomenclatures.dto.transcodage.SystemeCodageIdDTO;
import com.sqli.sante.nomenclatures.dto.transcodage.TranscodageRequestDTO;

@Stateless
@Remote(ElementClassifiableManagerService.class)
public class ElementClassifiableManagerServiceImpl implements ElementClassifiableManagerService {

    private static final Logger log = Logger.getLogger(ElementClassifiableManagerServiceImpl.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see com.sqli.sante.nomenclatures.service.ElementClassifiableManagerService#searchElementClassifiable(com.sqli.sante.nomenclatures.dto.elementclassifiable.ElementClassifiableRequestDTO)
     */
    @SuppressWarnings("deprecation")
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<ElementClassifiableDTO> searchElementClassifiable(ElementClassifiableRequestDTO pRequest)
            throws TechniqueException, UtilisationException {
        Nomenclature lNomenclature = NomenclatureBO.checkECRequestDTO(pRequest);
        // Setter le VERSION_ID de la nomenclature
        GererElementClassifiableBO.getNomenclatureVersionId(pRequest, lNomenclature);
        return GererElementClassifiableBO.searchEC(lNomenclature, pRequest, null, ElementClassifiableDTO.class);
    }

    /**
     * @see com.sqli.sante.nomenclatures.service.ElementClassifiableManagerService#searchElementClassifiable(com.sqli.sante.nomenclatures.dto.elementclassifiable.ElementClassifiableRequestDTO)
     */
    @SuppressWarnings("deprecation")
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<ElementClassifiableDTO> searchElementClassifiable(ElementClassifiableRequestDTO pRequest,
            ElementClassifiableSearchParamsDTO searchParams) throws TechniqueException, UtilisationException {
        Nomenclature lNomenclature = NomenclatureBO.checkECRequestDTO(pRequest);
        // Setter le VERSION_ID de la nomenclature
        GererElementClassifiableBO.getNomenclatureVersionId(pRequest, lNomenclature);
        return GererElementClassifiableBO.searchEC(lNomenclature, pRequest, searchParams, ElementClassifiableDTO.class);
    }

    /**
     * Service utilisé par le composant de recherche des nomenclatures
     */
    @Override
    @Deprecated
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<ElementClassifiableReponseDTO> searchNomenclatureElementsClassifiables(
            ComposantNomenclatureRequestDTO request) throws TechniqueException, UtilisationException {
        if (request != null && (request.getIdsElements() == null || request.getIdsElements().isEmpty())) {
            // Si n'est pas chargement des elements à partir de leurs identifiants on ne cherche que les EC actifs
            request.setActif(Boolean.TRUE);
        }
        ElementClassifiableRequestDTO lRequest = ElementClassifiableMapper.createElemnentClasifiableDTORequest(request);
        Nomenclature lNomenclature = NomenclatureBO.checkECRequestDTO(lRequest);
        // Setter le VERSION_ID de la nomenclature
        GererElementClassifiableBO.getNomenclatureVersionId(lRequest, lNomenclature);
        Collection<ElementClassifiableReponseDTO> elementsDtos = GererElementClassifiableBO.searchEC(lNomenclature,
                lRequest, null, ElementClassifiableReponseDTO.class);

        if (BooleanUtils.isNotTrue(request.getDoNotReturnIsFeuille())) {
            // on n'empeche pas la recherche pour mettre dans chaque objet s'il est feuille ou pas
            GererElementClassifiableBO.tagEC(elementsDtos, lNomenclature.getType(),
                    lRequest.getNomenclature().getVersionId(), lNomenclature.getCodeUnique());
        }
        return elementsDtos;
    }

    /**
     * recherches à partir du texte fourni et/ou dans les favoris et les listes
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public NomenclatureResponseDTO searchNomenclatures(NomenclatureRequestDTO request)
            throws TechniqueException, UtilisationException {
        NomenclatureResponseDTO response = new NomenclatureResponseDTO();
        Map<Long, List<ElementClassifiableIdDTO>> codesEcByVersion = null;
        if (request != null) {
            ComposantNomenclatureRequestDTO requestDTO = request.getRequest();
            if (requestDTO != null && (requestDTO.getIdsElements() == null || requestDTO.getIdsElements().isEmpty())) {
                // Si n'est pas chargement des elements à partir de leurs identifiants on ne cherche que les EC actifs
                requestDTO.setActif(Boolean.TRUE);
            }

            // Dans le cas de récuperations de élements d'une ou plusieurs versions par codes
            if (BooleanUtils.isTrue(requestDTO.getUseVersioning()) && requestDTO.getIdsElements() != null) {
                codesEcByVersion = new HashMap<Long, List<ElementClassifiableIdDTO>>();
                for (ElementClassifiableIdDTO eltId : requestDTO.getIdsElements()) {
                    if (eltId != null && StringUtils.isNotBlank(eltId.getCodeEC())) {
                        String[] verionAndCodeEc = eltId.getCodeEC().split("\\|");
                        Long lVersionId = verionAndCodeEc.length > 1 ? Long.parseLong(verionAndCodeEc[1].trim()) : null;
                        String codeEc = verionAndCodeEc[0];

                        if (codesEcByVersion.get(lVersionId) == null) {
                            List<ElementClassifiableIdDTO> codes = new ArrayList<ElementClassifiableIdDTO>();
                            codes.add(new ElementClassifiableIdDTO(codeEc, null, eltId.getDateEffective()));
                            codesEcByVersion.put(lVersionId, codes);
                        } else {
                            codesEcByVersion.get(lVersionId)
                                    .add(new ElementClassifiableIdDTO(codeEc, null, eltId.getDateEffective()));
                        }

                    }
                }
                if (!codesEcByVersion.isEmpty()) {
                    for (Long lVersionId : codesEcByVersion.keySet()) {
                        requestDTO.setIdsElements(codesEcByVersion.get(lVersionId));
                        if (lVersionId != null) {
                            requestDTO.getNomenclature().setVersionId(lVersionId);
                        }
                        if (response.getListNomenclatures() == null) {
                            response.setListNomenclatures(searchEcs(requestDTO, request));
                        } else {
                            response.getListNomenclatures().addAll(searchEcs(requestDTO, request));
                        }
                    }
                }
            } else {
                response.setListNomenclatures(searchEcs(requestDTO, request));
            }
        }

        return response;
    }

    private Collection<ElementClassifiableReponseDTO> searchEcs(ComposantNomenclatureRequestDTO requestDTO,
            NomenclatureRequestDTO request) throws TechniqueException, UtilisationException {

        /**
         * VARIABLE INITIALISATION START
         *******************************************************************************/
        // map id elements - element à retourner (favoris, listes et autres)
        Map<Long, ElementClassifiableReponseDTO> mapEcComplete = new HashMap<>();

        /**
         * frequences d'utilisation --> si jamais il a été demandé de ne chargé que les éléments ayant été fréquemment
         * utilisés SANS AUTRE CRITERE DE RECHERCHE DETERMINANTS on complètes la requête avec les id des EC fréquents
         */
        Collection<FrequenceUtilisationDTO> frequences = null;
        FrequenceUtilisationRequestDTO requestFrequenceUtil = new FrequenceUtilisationRequestDTO();
        requestFrequenceUtil.setUtilisateurId(request.getIdUser());
        requestFrequenceUtil.setTypeNomenclature(requestDTO.getNomenclature().getCodeLibre());

        if (request.getIdUser() != null && requestDTO.getChargerUniquementFrequents() != null
                && StringUtils.isBlank(requestDTO.getCode()) && StringUtils.isBlank(requestDTO.getLibelle())
                && (requestDTO.getIdsElements() == null || requestDTO.getIdsElements().isEmpty())) {
            // aucun critère de recherche déterminant (code, libelle & listIdEc NULL) --> on charge tous les fréquents
            // et on complète la request avec
            // les ids

            // recherche des fréquences
            frequences = getUtilisation(requestFrequenceUtil);
            if (frequences == null) {
                frequences = new ArrayList<FrequenceUtilisationDTO>();
            }

            // il a été demandé de charger tous les EC appartenant aux fréquents de l'utilisateur ==> on les
            // charge tous limités a ceux ayant la frequence d'utilisation minimale précisée
            requestDTO.setIdsElements(frequences.stream()
                    .filter(p -> p.getNbreUtilisation() >= requestDTO.getChargerUniquementFrequents())
                    .map(m -> m.getEc().getElementClassifiableId()).collect(Collectors.toList()));
            if (requestDTO.getIdsElements() == null || requestDTO.getIdsElements().isEmpty()) {
                // l'utilisateur ne possède aucun fréquent et aucun autre critère de recherche déterminant n'est
                // renseigné
                // --> on retourne une liste vide
                return new ArrayList<ElementClassifiableReponseDTO>();
            }
        }

        /**
         * map contenant les seuls éléments qu'on peut garder (ex : utilisation d'une sous liste, on ne garde que les
         * éléments de cette liste)
         */
        Map<Long, ElementClassifiableReponseDTO> mapEcFiltre = new HashMap<>();
        ElementClassifiableRequestDTO lRequest = ElementClassifiableMapper
                .createElemnentClasifiableDTORequest(requestDTO);
        Nomenclature nomenclature = NomenclatureBO.checkECRequestDTO(lRequest);
        // Version
        Long lVersionId = requestDTO.getNomenclature().getVersionId() != null
                ? requestDTO.getNomenclature().getVersionId()
                : GererElementClassifiableBO.getNomenclatureVersionId(requestDTO, nomenclature);
        requestDTO.getNomenclature().setVersionId(lVersionId);
        lRequest.setNomenclature(requestDTO.getNomenclature());
        String libelleRecherche = lRequest != null ? lRequest.getLibelle() : null;
        // etoile jaune : return uniquement les favoris utilisateur
        boolean rechercheOnlyFavoris = BooleanUtils.isTrue(requestDTO.getReturnAllFavoriUser());
        ;
        // Etoile rouge : Recherche les listes restrictives :return les elements des listes uniquement
        boolean rechercheOnlyListes = BooleanUtils.isTrue(requestDTO.getReturnAllFavoriPublic());
        boolean returnExceptionIfTooManyResult = BooleanUtils.isTrue(requestDTO.getReturnExceptionIfTooManyResult());
        boolean isSousListe = requestDTO.getListRequest() != null && requestDTO.getListRequest().getCodes() != null
                && !requestDTO.getListRequest().getCodes().isEmpty();
        // recherche des éléments par défaut, si sous liste cherché il faut récupérer tous ses éléments
        boolean rechercheDefaut = isSousListe || (!rechercheOnlyListes && !rechercheOnlyFavoris);
        // recherche des éléments : si pas de libellés, tous les favoris sont retournés
        // si libellé, recherche normale puis filtre sur les favoris
        Long idEtabCourant = request.getIdEtablissement() != null ? request.getIdEtablissement() : null;
        Long idUfCourante = request.getIdUniteFonctionnelle() != null ? request.getIdUniteFonctionnelle() : null;
        Long idUser = request.getIdUser() != null ? request.getIdUser() : null;
        // not null pour comparaison après
        String nnIdUser = idUser != null ? idUser.toString() : "";
        String nnIdEtab = idEtabCourant != null ? idEtabCourant.toString() : "";
        String nnIdUf = idUfCourante != null ? idUfCourante.toString() : "";
        String nnActiviteId = "";
        String nnDisciplineId = "";
        String nnContexteUtilisation = requestDTO.getContexteUtilisation() != null
                ? requestDTO.getContexteUtilisation().getCode() : "";
        Collection<ElementClassifiableReponseDTO> listeEcComplete = new ArrayList<>();
        Set<Long> lvUserFavoriteECIds = null;

        /** VARIABLE INITIALISATION END ********************************************************************/

        /**
         * DEFAULT SEARCH (USING SEARCH TEXT)
         ******************************************************************************/
        if (rechercheDefaut) {
            // recherche normale avec un texte de recherche
            ElementClassifiableServiceManagerBO.defaultSearch(requestDTO, request, mapEcComplete, mapEcFiltre, lRequest,
                    nomenclature, rechercheOnlyFavoris, rechercheOnlyListes, returnExceptionIfTooManyResult,
                    isSousListe);
        }

        /**
         * SEARCH FAVORIS + LISTE + GET SYNONYMES
         **************************************************************************/
        Collection<Synonyme> listeSynonymes = new ArrayList<>();
        HashMap<Long, Synonyme> mapIdSynonyme = new HashMap<>();
        // recherche des favoris //
        String typeNomenclature = null;

        typeNomenclature = ElementClassifiableServiceManagerBO.getTypeNomenclatureFromRequestDTO(requestDTO);

        if (typeNomenclature != null) {
            NomenclatureAppliquable n = NomenclatureAppliquable.getNomenclatureAppliquable(typeNomenclature);

            if (rechercheOnlyListes) {
                // recherche listes
                long t = System.currentTimeMillis();
                searchListes(mapEcComplete, mapEcFiltre, true, nnActiviteId, nnDisciplineId, nnIdEtab, nnIdUf,
                        nnContexteUtilisation, n, lRequest);
                log.debug("-Nomenclature : recherche listes en " + (System.currentTimeMillis() - t) + " ms");
            }

            if (isSousListe || rechercheOnlyFavoris) {
                // favoris ne sert plus à filtrer sauf si on veut récupérer que les favoris (bouton jaune du
                // composant),c'est uniquement pour trier
                long tf = System.currentTimeMillis();
                lvUserFavoriteECIds = searchFavoris(mapEcComplete, mapEcFiltre, idUser, request.getUserType(), rechercheOnlyFavoris, nnIdUser, nnIdEtab, nnIdUf,
                        nnContexteUtilisation, n, lVersionId, lRequest);
                log.debug("-Nomenclature : recherche favoris en " + (System.currentTimeMillis() - tf) + " ms");

            }
            
            if (BooleanUtils.isTrue(lRequest.getActivateSynonyme()) && StringUtils.isNotBlank(libelleRecherche)) {
                // search Synonymes
                listeSynonymes = ElementClassifiableServiceManagerBO.getSynonymesByLibelle(libelleRecherche,
                        mapIdSynonyme, n, lRequest);
                listeEcComplete = ElementClassifiableServiceManagerBO.getOrUpdateECsBySynonyme(mapEcComplete,
                        mapIdSynonyme);
                listeEcComplete = listeEcComplete == null ? new ArrayList<>() : listeEcComplete;
            } else if (mapEcComplete != null && !mapEcComplete.isEmpty()) {
                Collection<ElementClassifiableReponseDTO> tmp = mapEcComplete.values();
                listeEcComplete.addAll(tmp);
            }

        }

        // ajout des infos feuille sur chaque élément
        if (Boolean.TRUE.equals(nomenclature.getIsPlate())) {
            // il faut mettre le bon type qui sert pour trouver la dao...
            nomenclature.setType(GestionNomenclaturesConstantes.CODE_NOMENCLATURE_PLATE);
            // traitement du cas nomenclature plate
        }
        /** MISE A JOUR DES "PSEUDO_LIBELLE" DES ECs EN SE BASANT SUR LES SYNONYMES **/
        
        

        // Inclure les raccourcis des élements classifiables dans les résultats retournés ?
        Collection<Raccourci> raccourcis = getRaccourcis(requestDTO, libelleRecherche, typeNomenclature, lVersionId);
        if (raccourcis != null && !raccourcis.isEmpty()) {
            // Convertir les raccourcis trouvés en ElementClassifiableReponseDTO et les inclure dans les résultats de
            // recherche
            listeEcComplete.addAll(ElementClassifiableMapper
                    .createElementsClassifiablesReponseDTOFromRaccourci(raccourcis, nomenclature.getPseudoLibelle()));
        }

        /** SEARCH PROTOCOLE *******/
        if (BooleanUtils.isTrue(lRequest.getActivateProtocole())) {

            Optional<Collection<Protocole>> lvProtocoles = ElementClassifiableServiceManagerBO
                    .searchProtocoles(lRequest, Boolean.TRUE);

            if (lvProtocoles.isPresent()) {
                listeEcComplete.addAll(ElementClassifiableMapper
                        .createElementsClassifiablesReponseDTOFromProtocole(lvProtocoles.get()));
            }
        }

        /**********************************************************************************
         * PERFORM SEARCH BASED ON SYNONYMES											**
         *********************************************************************************/
        if (BooleanUtils.isTrue(lRequest.getActivateSynonyme())
                && (listeSynonymes != null && !listeSynonymes.isEmpty())) {

            Optional<Collection<ElementClassifiableReponseDTO>> lvSearchRes = ElementClassifiableServiceManagerBO
                    .searchECsBySynonymes(lVersionId, listeSynonymes, typeNomenclature, listeEcComplete);
            if (lvSearchRes.isPresent()) {
                listeEcComplete.addAll(lvSearchRes.get());
            }
        }
        /**
         * END PERFORM SEARCH BASED ON SYNONYMES
         ********************************************************************************/

        /*************************************************************************************************
         * GET FAVORIS PUBLIC EC (FILTRE_FAVORIS_PUBLIC = TRUE) + TAG ELEMENT BY FAVORIS PUBLIC OR USER **
         *************************************************************************************************/
        Optional<Collection<AssFavorisEntity>> lvAssEntFav = ElementClassifiableServiceManagerBO
                .getAssFavorisEntityByContext(request, nomenclature, nnContexteUtilisation);
        
        

        if (lvAssEntFav.isPresent()) {
            Optional<Collection<ElementClassifiableReponseDTO>> lvAFEECs = FavoriUserBO
                    .getECsFromFavorisIds(typeNomenclature, lVersionId, lvAssEntFav.get());
            // Optional<Collection<ElementClassifiable>> lvAFEECs = FavoriUserBO.getECsFromFavorisIds(typeNomenclature,
            // lVersionId, lvAFEFavoris);
            if (lvAFEECs.isPresent()) {
            	
                /** GET FAVORIS PUBLIC ONLY **/
                if (BooleanUtils.isTrue(requestDTO.getFiltreFavoriPublic())
                        && BooleanUtils.isTrue(requestDTO.getReturnAllFavoriPublic())) {

                    listeEcComplete.addAll(lvAFEECs.get());
                }
                /** TAG PUBLIC FAVORIS ECs **/
                if (BooleanUtils.isTrue(requestDTO.getReturnIsFavoriUser())) {
                    listeEcComplete = ElementClassifiableServiceManagerBO.tagPublicFavorisECsWithDTO(lVersionId,
                            typeNomenclature, listeEcComplete, lvAFEECs.get());
                }
            }
        }
        
        /****************************************************************************
         **			 END TAG ELEMENT BY FAVORIS PUBLIC OR USER					   **		
         ****************************************************************************/
        
        boolean isECListNotEmpty = (listeEcComplete != null && !listeEcComplete.isEmpty());
        
        /*************************************************************************************************
         * FILTER BASED ON SPECIFY USER FAVORITE EC (ONLY IF FILTRE_FAVORIS_USER = TRUE) **
         *************************************************************************************************/
        
        if(BooleanUtils.isTrue(requestDTO.getFiltreFavoriUser()) && (BooleanUtils.isFalse(requestDTO.getReturnAllFavoriUser() == null || requestDTO.getReturnAllFavoriUser())) && isECListNotEmpty){
        	Set<Long> lvUserFavECs = lvUserFavoriteECIds != null ? lvUserFavoriteECIds : ElementClassifiableServiceManagerBO.getUserFavoriteECIds(idUser, request.getUserType(), typeNomenclature, lVersionId, lRequest);
        	listeEcComplete = listeEcComplete.stream().filter(value -> lvUserFavECs.contains(value.getElementClassifiableId().getIdEc())).collect(Collectors.toList());
        }
        
        /*************************************************************************************************
         * 				END USER FAVORITE EC FILTER																	**
         *************************************************************************************************/

        if (request.getIdUser() != null && (BooleanUtils.isTrue(requestDTO.getChargerFrequencesUtilisation())
                || requestDTO.getChargerUniquementFrequents() != null)) {
            /**
             * filtre par fréquence utilisation ajout des infos de fréquence
             */
            listeEcComplete = filtrerParFrequenceUtilisation(listeEcComplete, requestFrequenceUtil, requestDTO,
                    frequences);
        }
        
        
        /************************************************************
         * 		ADD RELATION TO RESPONSE OBJECT					   **
         ************************************************************/
        if(requestDTO != null && BooleanUtils.isTrue(requestDTO.getIncludeRelationsInResponse()) && isECListNotEmpty){

            listeEcComplete = ElementClassifiableServiceManagerBO.associateRelations(nomenclature.getLibelleCourt(), 
            		(requestDTO.getTypeRelations() != null && !requestDTO.getTypeRelations().isEmpty()) ? Optional.of(requestDTO.getTypeRelations()) : Optional.empty(),
                    listeEcComplete);

        }
        
        /************************************************************
         * 		SET RESPONSE FOR EACH RESPONSE OBJECT			  **
         ************************************************************/
        if(requestDTO != null && BooleanUtils.isTrue(requestDTO.getIncludeGroupesInResponse()) && isECListNotEmpty){
        	listeEcComplete = ElementClassifiableServiceManagerBO.associateGroupes(nomenclature.getLibelleCourt(), listeEcComplete);
        }
        
        /************************************************************************************************************************************************
         * 		TAG ECs PAR "FEUILLE" ou pas => ("NOEUD") / TOUS CECI CONDITIONNE PAR L'ATTRIBUT "doNotReturnIsFeuille => false"  					   **
         ************************************************************************************************************************************************/
        /*if (BooleanUtils.isNotTrue(requestDTO.getDoNotReturnIsFeuille())) {
            // on n'empeche pas la recherche pour mettre dans chaque objet s'il est feuille ou pas
            String lNomenclatureCode = Boolean.TRUE.equals(nomenclature.getIsPlate()) ? NomenclatureEnum.PLATE.getCode()
                    : nomenclature.getLibelleCourt();
            GererElementClassifiableBO.tagEC(listeEcComplete, lNomenclatureCode,
                    requestDTO.getNomenclature().getVersionId(), nomenclature.getCodeUnique());
        }*/
        
        return listeEcComplete;
    }

    /**
     * méthode permettant de filtrer la liste originale d'EC en fonction de la frequence d'utilisation utilisateur
     * 
     * @param listeOriginale : liste à filtrer
     * @param requestFrequenceUtil : demande de filtre
     * @param requestDTO : autres critères de recherche
     * @param frequencesDejaChargees : frequences d'utilisation (si déjà chargée sinon null)
     * @return la liste originale filtrée. Aucun filtre n'est appliqué si l'utilisateur n'a aucun fréquent...
     * @throws UtilisationException
     * @throws TechniqueException
     */
    private Collection<ElementClassifiableReponseDTO> filtrerParFrequenceUtilisation(
            Collection<ElementClassifiableReponseDTO> listeOriginale,
            FrequenceUtilisationRequestDTO requestFrequenceUtil, ComposantNomenclatureRequestDTO requestDTO,
            Collection<FrequenceUtilisationDTO> frequencesDejaChargees)
            throws UtilisationException, TechniqueException {
        /**
         * ajout des informations concernant les fréquences d'utilisation
         */
        if (frequencesDejaChargees == null) {
            frequencesDejaChargees = getUtilisation(requestFrequenceUtil);
        }
        setFrequenceUtilisation(listeOriginale, frequencesDejaChargees);

        if (requestDTO.getChargerUniquementFrequents() != null) {
            // si demandé, on ne retourne que les éléments avec la fréquence minimale demandée
            return listeOriginale.stream()
                    .filter(p -> p.getNombreUtilisationUtilisateur() > 0
                            && p.getNombreUtilisationUtilisateur() >= requestDTO.getChargerUniquementFrequents())
                    .collect(Collectors.toList());
        } else {
            return listeOriginale;
        }
    }

    /**
     * affecte les frequences d'utilisation aux ElementClassifiableReponseDTO à partir de la collection des fréquences
     * 
     * @param listeEcComplete : contient les ECs dont il faut compléter les fréquences d'utilisation
     * @param frequences : collection des fréquences par élément classifiable
     */
    private void setFrequenceUtilisation(Collection<ElementClassifiableReponseDTO> listeEcComplete,
            Collection<FrequenceUtilisationDTO> frequences) {
        for (ElementClassifiableReponseDTO element : listeEcComplete) {
            FrequenceUtilisationDTO frequence = null;
            if (frequences != null) {
                frequence = frequences.stream().filter(p -> element.getCode().equals(p.getEc().getCode())).findAny()
                        .orElse(null);
            }
            if (frequence != null) {
                if (frequence.getNbreUtilisation() != null) {
                    element.setNombreUtilisationUtilisateur(new Long(frequence.getNbreUtilisation().intValue()));
                } else {
                    element.setNombreUtilisationUtilisateur(0L);
                }
            } else {
                element.setNombreUtilisationUtilisateur(0L);
            }
        }
    }

    /**
     * split la chaine selon le caractère ";"
     *
     * @param string
     * @return
     */
    private List<String> splitMultiVal(String val) {
        List<String> liste = new ArrayList<String>();

        if (val != null) {
            String[] tab = val.split(";");
            for (String s : tab) {
                if (StringUtils.isNotBlank(s) && !"null".equals(s)) {
                    liste.add(s);
                }
            }
        }
        return liste;
    }

    /**
     * recherche les éléments des favoris
     *
     * @param mapEcComplete map à compléter ou à remplir avec les éléments trouvés
     * @param mapEcFiltre
     * @param rechercheDefaut true si on a fait la recherche par défaut (si true, alors on n'ajoute pas les favoris non
     *            présents dans la mapEcComplete)
     * @param nnActiviteId
     * @param nnDisciplineId
     * @param nnEtabId
     * @param n
     * @param lRequest
     * @throws TechniqueException
     * @throws UtilisationException
     */
    @SuppressWarnings("unchecked")
    private void searchListes(Map<Long, ElementClassifiableReponseDTO> mapEcComplete,
            Map<Long, ElementClassifiableReponseDTO> mapEcFiltre, boolean canAddElement, String nnActiviteId,
            String nnDisciplineId, String nnEtabId, String nnUfId, String nnContexteUtilisation,
            NomenclatureAppliquable n, ElementClassifiableRequestDTO lRequest)
            throws TechniqueException, UtilisationException {

        if (nnEtabId != null || nnUfId != null) {
            canAddElement = true;
        }
        // on passe aussi le libellé de recherche pour filtrer lors de la requete et l'accélerer (éviter de parcourir
        // les 19000 résultats de listes hypothèses & diag)
        String libelleRecherche = lRequest != null ? lRequest.getLibelle() : null;

        // listes associées à l'étab ou l'uf
        Collection<Liste> listes = GestionListeBO.listerListe(null, n.getNomenclatureCode(), n.getNomenclatureId(),
                null);
        boolean isClear = false;
        if (listes != null && !listes.isEmpty()) {
            for (Liste liste : listes) {
                // chaque liste
                Long idListe = liste.getListeId();

                long t;

                // la liste peut être liée à un etab,activite, discipline (table ENTITE_FILTRE[_PLATE]
                // Filtre : ça DOIT correspondre (quelques ms)
                Map<String, String> mapCriteria = GestionListeBO.getMapCriteia(n.getNomenclatureCode(),
                        n.getNomenclatureId(), idListe);

                List<String> lEtabId = new ArrayList<String>();
                List<String> lActiviteId = new ArrayList<String>();
                List<String> lDisciplineId = new ArrayList<String>();
                List<String> lContexteUtilisation = new ArrayList<String>();
                if (mapCriteria != null) {
                    lEtabId = splitMultiVal(mapCriteria.get(TitreCritereEnum.ETABLISSEMENT.getTitle()) != null
                            ? mapCriteria.get(TitreCritereEnum.ETABLISSEMENT.getTitle()) : "");
                    lActiviteId = splitMultiVal(mapCriteria.get(TitreCritereEnum.ACTIVITE.getTitle()) != null
                            ? mapCriteria.get(TitreCritereEnum.ACTIVITE.getTitle()) : "");
                    lDisciplineId = splitMultiVal(mapCriteria.get(TitreCritereEnum.DISCIPLINE.getTitle()) != null
                            ? mapCriteria.get(TitreCritereEnum.DISCIPLINE.getTitle()) : "");
                    lContexteUtilisation = splitMultiVal(
                            mapCriteria.get(TitreCritereEnum.CONTEXTE_UTILISATION.getTitle()) != null
                                    ? mapCriteria.get(TitreCritereEnum.CONTEXTE_UTILISATION.getTitle()) : "");
                }

                // tests filtres : param&bdd doivent être identiques (les 2 vide ou les 2 égaux) pour les garder
                // si c'est configuré, il faut que ce soit pareil

                if (listeNonVide(lEtabId) && !valNullOuIdentiques(nnEtabId, lEtabId)) {
                    // if ((StringUtils.isBlank(nnEtabId) && lEtabId.isEmpty())
                    // || (StringUtils.isNotBlank(nnEtabId) && lEtabId.contains(nnEtabId))) {
                    // etab défini, et différent du courant
                    log.debug("Nomenclature : recherche liste : liste " + liste.getListeId()
                            + " non utilisée car étab différent de " + nnEtabId + " (" + liste.getLibelle() + ")");
                    continue;
                }
                // if (StringUtils.isNotBlank(nnActiviteId) && !lActiviteId.contains(nnActiviteId)) {
                if (listeNonVide(lActiviteId) && !valNullOuIdentiques(nnActiviteId, lActiviteId)) {
                    // activite définie, et différente du courant
                    log.debug("Nomenclature : recherche liste : liste " + liste.getListeId()
                            + " non utilisée car activité différente de " + nnActiviteId + " (" + liste.getLibelle()
                            + ")");
                    continue;
                }
                // if (StringUtils.isNotBlank(nnDisciplineId) && !lDisciplineId.contains(nnDisciplineId)) {
                if (listeNonVide(lDisciplineId) && !valNullOuIdentiques(nnDisciplineId, lDisciplineId)) {
                    // discipline définie, et différente du courant
                    log.debug("Nomenclature : recherche liste : liste " + liste.getListeId()
                            + " non utilisée car discipline différente de " + nnDisciplineId + " (" + liste.getLibelle()
                            + ")");
                    continue;
                }
                // if (StringUtils.isNotBlank(nnContexteUtilisation)
                // && !lContexteUtilisation.contains(nnContexteUtilisation)) {

                if (StringUtils.isNotBlank(nnContexteUtilisation) && !listeNonVide(lContexteUtilisation)) {
                    // si on passe un contexte il faut forcément que ca corresponde
                    log.debug("Nomenclature : recherche liste : liste " + liste.getListeId()
                            + "  non utilisée car contexte d'utilisation fourni différent de " + nnContexteUtilisation
                            + " (" + liste.getLibelle() + ")");
                    continue;
                }
                if (listeNonVide(lContexteUtilisation)
                        && !valNullOuIdentiques(nnContexteUtilisation, lContexteUtilisation)) {
                    // discipline définie, et différente du courant
                    log.debug("Nomenclature : recherche liste : liste " + liste.getListeId()
                            + "  non utilisée car contexte d'utilisation différent de " + nnContexteUtilisation + " ("
                            + liste.getLibelle() + ")");
                    continue;
                }
                log.debug("Nomenclature : recherche liste : liste " + liste.getListeId()
                        + " conservée selon les critères liés (" + liste.getLibelle() + ")");

                // CA PEUT correspondre (on prend dans l'ordre : lié selon uf puis étab puis à rien)
                // association à des étabs/uf (2ms)
                Map<String, Object> associations = AssListeEntityBO.getAllListeAssociations(n.getNomenclatureCode(),
                        n.getNomenclatureId(), idListe, true);

                Map<String, List<AssListeEntity>> listeAssocsByEtab = (Map<String, List<AssListeEntity>>) associations
                        .get(AssListeEntityBO.ENTITIES);

                // si on doit garder la liste
                boolean listeConservee = false;

                Long poids = 0L;
                if (listeAssocsByEtab != null) {
                    for (Entry<String, List<AssListeEntity>> a : listeAssocsByEtab.entrySet()) {
                        for (AssListeEntity entite : a.getValue()) {
                            // log.debug("Assoc : "
                            // + a.getKey()
                            // + " => "
                            // + (entite.getIdeomedEntityId() != null ? "ETAB " + entite.getIdeomedEntityId()
                            // : (entite.getAllEtabs() ? "ALL_ETAB" : "")) + " / UF "
                            // + entite.getIdeomedUfId());

                            if (StringUtils.isNotBlank(entite.getIdeomedUfId())
                                    && nnUfId.equals(entite.getIdeomedUfId())) {
                                // lié à l'uf directement
                                if (poids < 50L) {
                                    poids = 50L;
                                }
                                listeConservee = true;
                                break;
                            } else if (StringUtils.isNotBlank(entite.getIdeomedEntityId())
                                    && nnEtabId.equals(entite.getIdeomedEntityId())
                                    && BooleanUtils.isTrue(entite.getUfs()) // coché "toutes les uf" pour l'étab
                            ) {
                                // lié à l'etab et toutes les uf
                                if (poids < 45L) {
                                    poids = 45L;
                                }
                                listeConservee = true;
                                break;
                            } else if (BooleanUtils.toBoolean(entite.getAllEtabs())) {
                                // lié à tous les etabs
                                if (poids < 40L) {
                                    poids = 40L;
                                }
                                listeConservee = true;
                                break;
                            }

                        }
                    }
                }

                if (listeConservee) {
                    log.debug("Nomenclature : recherche liste : liste " + liste.getListeId()
                            + " conservée selon les associations etab/uf ");

                    // on cherche les éléments de la liste
                    boolean includeCode = lRequest != null && lRequest.getIncludeCode() != null
                            ? lRequest.getIncludeCode() : false;
                    t = System.currentTimeMillis();
                    Collection<ElementClassifiable> elementClassifiableByList = GestionListeBO
                            .getElementClassifiableByList(n.getNomenclatureCode(), null, liste.getListeId(),
                                    libelleRecherche, includeCode);

                    Set<String> lCodesECs = null;
                    List<Long> lVersionIds = new ArrayList<Long>();
                    lVersionIds.add(lRequest.getNomenclature().getVersionId());
                    if (elementClassifiableByList != null && !elementClassifiableByList.isEmpty()) {
                        lCodesECs = new HashSet<String>();
                        for (ElementClassifiable lElementClassifiable : elementClassifiableByList) {
                            lCodesECs.add(lElementClassifiable.getCode());
                        }
                        elementClassifiableByList = GererElementClassifiableBO
                                .getElementsClassifiablesByCodesAndVersionIds(lCodesECs, n.getNomenclatureCode(),
                                        lVersionIds, null);
                    }

                    log.debug("Nomenclature : recherche éléments liste en " + (System.currentTimeMillis() - t) + " ms");
                    if (lRequest.getUseVersioning()) {
                        if (elementClassifiableByList != null) {
                            for (ElementClassifiable lEc : elementClassifiableByList) {
                                lEc.setCode(lEc.getCode() + GestionNomenclaturesConstantes.CODE_VERSION_SEP
                                        + lEc.getVersionId());
                            }
                        }
                    }
                    t = System.currentTimeMillis();
                    Collection<ElementClassifiableReponseDTO> listeEC = ElementClassifiableMapper
                            .createElementClasfiableReponseDTOs(elementClassifiableByList);
                    log.debug("Nomenclature : mapping éléments liste en " + (System.currentTimeMillis() - t) + " ms");
                    if (listeEC != null) {
                        if (!isClear) {
                            mapEcComplete.clear();
                            isClear = true;
                        }
                        for (ElementClassifiableReponseDTO newEc : listeEC) {
                            // chaque élément de la liste

                            ElementClassifiableReponseDTO oldEc = mapEcComplete
                                    .get(newEc.getElementClassifiableId().getIdEc());
                            if (oldEc != null) {
                                // element deja dans la liste à retourner
                                if (oldEc.getFavoriPoidsPrecision() == null
                                        || (poids != null && poids > oldEc.getFavoriPoidsPrecision())) {
                                    // poids plus important, on met à jour l'élément existant
                                    oldEc.setFavoriPoidsPrecision(poids);
                                }
                                oldEc.setIsFavoriPublic(true);
                            } else if (canAddElement) {

                                if (mapEcFiltre == null || mapEcFiltre.isEmpty()
                                        || mapEcFiltre.containsKey(newEc.getElementClassifiableId().getIdEc())) {
                                    // ajout possible que si 1.pas de liste filtree OU 2.l'élément est dans la liste
                                    // filtrée
                                    // ajout que si on filtre que sur les favoris/listes
                                    // donnée sur l'élément qui proviennent de la liste
                                    newEc.setIsFavoriPublic(true);
                                    newEc.setFavoriPoidsPrecision(poids);

                                    // ajout
                                    mapEcComplete.put(newEc.getElementClassifiableId().getIdEc(), newEc);
                                }
                            }
                        }
                    }
                } else {
                    // si liste liée à rien, on passe
                    log.debug("Nomenclature : recherche liste : liste " + liste.getListeId()
                            + " non conservée selon les associations etab/uf (" + liste.getLibelle() + ")");
                }
            }
        }
    }

    /**
     * liste non nulle et non vide
     */
    private boolean listeNonVide(List<String> liste) {
        return liste != null && !liste.isEmpty();
    }

    /**
     * retourne true si : <br>
     * - param vide et liste null ou vide <br>
     * - liste contient param
     */
    private boolean valNullOuIdentiques(String param, List<String> liste) {
        if (StringUtils.isBlank(param) && (liste == null || liste.isEmpty())) {
            return true;
        }
        if (StringUtils.isNotBlank(param) && liste != null && liste.contains(param)) {
            return true;
        }
        return false;
    }

    /**
     * recherche les éléments des favoris
     *
     * @param mapEcComplete map à compléter ou à remplir avec les éléments trouvés
     * @param mapEcFiltre
     * @param idUser
     * @param rechercheDefaut true si on a fait la recherche par défaut (si true, alors on n'ajoute pas les favoris non
     *            présents dans la mapEcComplete)
     * @param nnIdUser
     * @param nnIdUf
     * @param pNomenclature
     * @param pRequest
     * @throws TechniqueException
     * @throws UtilisationException
     */
    @SuppressWarnings("unused")
	private Set<Long> searchFavoris(Map<Long, ElementClassifiableReponseDTO> mapEcComplete,
            Map<Long, ElementClassifiableReponseDTO> mapEcFiltre, Long idUser,UserTypeEnum pvTypeUser, boolean canAddElement, String nnIdUser,
            String pIdEtab, String nnIdUf, String pContexteUtilisation, NomenclatureAppliquable pNomenclature,
            Long pVersionId, ElementClassifiableRequestDTO pRequest) throws TechniqueException, UtilisationException {

        Long poids = 140L;
        boolean isFavoriUser = true;
        Set<Long> lvUserFavorisECIds = null;

        /** TAG FAVORIS USER + FAVORIS GROUPS ASSOCIATED TO THE USER **/
        Optional<Collection<UtilisateursGroupesFavoris>> lvUserAndItGroupesFav = Optional
                .of(new ArrayList<UtilisateursGroupesFavoris>());
        
        if (idUser != null) {
        	
        	if( UserTypeEnum.SIMPLE_USER.getUserType().equals(pvTypeUser.getUserType()) && pRequest.getIncludeUserGroupesFavoris()){
        		
        		lvUserAndItGroupesFav = UtilisateursGroupesFavorisBo.getUserAndItGroupeFavorisByUserId(
                        pNomenclature.getNomenclatureCode(), idUser); 
        	}else {
        		
        		lvUserAndItGroupesFav = UtilisateursGroupesFavorisBo.getByIdsAndTypes(
        				new HashSet<>(Arrays.asList(idUser)), Arrays.asList(pvTypeUser.getUserType()), 
        				pNomenclature.getNomenclatureCode(), null);
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
                .getECsFromFavorisIds(pNomenclature.getNomenclatureCode(), pVersionId, lvUserAndItGroupesFavIds);

        if (lvUserGroupFavEcs.isPresent() && !lvUserGroupFavEcs.get().isEmpty()) {
        	
        	/** User's Favorite Element Classsifiable Ids  **/
        	lvUserFavorisECIds = lvUserGroupFavEcs.get().stream().map(ElementClassifiable::getElementClassifiableId).collect(Collectors.toSet());

            Collection<ElementClassifiable> lvUserGroupFavECVal = lvUserGroupFavEcs.get();

            if (pRequest.getUseVersioning()) {

                lvUserGroupFavECVal = lvUserGroupFavECVal.stream().map(value -> {
                    ElementClassifiable lvTmpEC = value;
                    lvTmpEC.setCode(
                            value.getCode() + GestionNomenclaturesConstantes.CODE_VERSION_SEP + value.getVersionId());
                    return lvTmpEC;
                }).collect(Collectors.toList());
            }

            /** CREATE RESPONSE DTO AND TAG ECs () **/
            Collection<ElementClassifiableReponseDTO> lvUserGroupFavECResponseDTO = ElementClassifiableMapper
                    .createElementClasfiableReponseDTOs(lvUserGroupFavECVal);
            lvUserGroupFavECResponseDTO.stream().forEach(
                    value -> tagAndSetPoids(mapEcComplete, mapEcFiltre, canAddElement, poids, isFavoriUser, value));
        }
        
        return lvUserFavorisECIds;
    }

    /**
     * @param mapEcComplete
     * @param mapEcFiltre
     * @param canAddElement
     * @param poids
     * @param isFavoriUser
     * @param newEc
     */
    private void tagAndSetPoids(Map<Long, ElementClassifiableReponseDTO> mapEcComplete,
            Map<Long, ElementClassifiableReponseDTO> mapEcFiltre, boolean canAddElement, Long poids,
            boolean isFavoriUser, ElementClassifiableReponseDTO newEc) {

        ElementClassifiableReponseDTO oldEc = mapEcComplete.get(newEc.getElementClassifiableId().getIdEc());

        if (oldEc != null) {
            if (oldEc.getFavoriPoidsPrecision() == null || (poids != null && poids > oldEc.getFavoriPoidsPrecision())) {
                // poids plus important, on met à jour l'élément existant
                oldEc.setFavoriPoidsPrecision(poids);
            }
            oldEc.setIsFavoriUser(isFavoriUser);
        } else if (canAddElement) {
            // ajout que si on filtre que sur les favoris/listes
            if (mapEcFiltre == null || mapEcFiltre.isEmpty()
                    || mapEcFiltre.containsKey(newEc.getElementClassifiableId().getIdEc())) {
                // ajout possible que si 1.pas de liste filtree OU 2.l'élément est dans la liste filtrée
                // donnée sur l'élément qui proviennent de la liste de favoris
                newEc.setIsFavoriUser(isFavoriUser);
                newEc.setFavoriPoidsPrecision(poids);
                // ajout
                mapEcComplete.put(newEc.getElementClassifiableId().getIdEc(), newEc);
            }
        }
    }

    /*
     *
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<NomenclatureDTO> searchNomenclatureTypes(TypeNomenclatureRequestDTO request)
            throws TechniqueException, UtilisationException {
        List<NomenclatureDTO> nomenclatureFounds = null;
        if (request != null) {
            request.setActif(Boolean.TRUE);
            Collection<Nomenclature> lNomenclatures = NomenclatureBO.searchNomenclature(request);
            nomenclatureFounds = NomenclatureMapper.getListDtoFromListModel(lNomenclatures);
        }
        return nomenclatureFounds;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<ECMetadonneeDTO> getECMetadonnees(ElementClassifiableIdDTO elementClassifiableIdDTO,
            NomenclatureIdDTO typeNomenclature) throws TechniqueException, UtilisationException {
        Long idEc = elementClassifiableIdDTO.getIdEc();
        if (idEc == null) {
            // Recherche par code EC
            ElementClassifiableRequestDTO request = new ElementClassifiableRequestDTO();
            request.setCode(elementClassifiableIdDTO.getCodeEC());
            request.setNomenclature(typeNomenclature);
            if (typeNomenclature.getVersion() == null) {
                Nomenclature lNomenclature = NomenclatureBO.checkECRequestDTO(request);
                GererElementClassifiableBO.getNomenclatureVersionId(request, lNomenclature);
            }
            Collection<ElementClassifiableDTO> searchElementClassifiable = searchElementClassifiable(request);
            if (searchElementClassifiable != null && !searchElementClassifiable.isEmpty()) {
                ElementClassifiableDTO elementClassifiableDTO = searchElementClassifiable.iterator().next();
                idEc = elementClassifiableDTO.getElementClassifiableId().getIdEc();
            }
        }
        return GererElementClassifiableBO.getECMetadonnees(idEc, typeNomenclature.getCodeLibre());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<ElementClassifiableDTO> searchCodeExterne(ElementClassifiableIdDTO elementClassifiableIdDTO,
            NomenclatureIdDTO nomenclature, SystemeCodageIdDTO systemCodage)
            throws TechniqueException, UtilisationException {
        // System de codage
        ISystemeCodageService lSystemeCodageService = new SystemeCodageServiceImpl();
        SystemeCodage lSystemeCodage = new SystemeCodage();
        lSystemeCodage.setCode(systemCodage.getCodeSC());
        lSystemeCodage = lSystemeCodageService.findSystemeCodage(lSystemeCodage);
        if (lSystemeCodage == null) {
            log.error("Aucune systéme de codage avec le code : " + systemCodage.getCodeSC() + " n'a été trouvé !");
            return null;
        }
        // Nomenclature
        Nomenclature lNomencalture = new Nomenclature();
        lNomencalture.setLibelleCourt(
                nomenclature.getCode() != null ? nomenclature.getCode().getTitle() : nomenclature.getCodeLibre());
        Collection<Nomenclature> lNomenclatureList = NomenclatureBO.getNomenclature(lNomencalture, null);
        if (lNomenclatureList == null || lNomenclatureList.isEmpty()) {
            log.error("Aucune nomenclature avec le code : " + lNomencalture.getLibelleCourt() + " n'a été trouvée !");
            return null;
        }
        lNomencalture = lNomenclatureList.iterator().next();
        String lCodeNomenclature = lNomencalture.getLibelleCourt();
        Long lVersionId = null;
        if (nomenclature.getVersion() == null) {
            lVersionId = NomenclatureVersionBo.getLastVersionIdByNomenclatureId(lNomencalture.getNomenclatureId());
        } else {
            NomenclatureVersion lNomenclatureVersion = new NomenclatureVersion();
            lNomenclatureVersion.setVersion(nomenclature.getVersion());
            lNomenclatureVersion.setNomenclatureId(lNomencalture.getId());
            Collection<NomenclatureVersion> lNomenclatureVersions = NomenclatureVersionBo
                    .searchNomenclatureVersion(lNomenclatureVersion, null);
            if (lNomenclatureVersions == null || lNomenclatureVersions.isEmpty()) {
                throw new UtilisationException(
                        "Aucune nomenclature n'a été trouvé pour la version : " + nomenclature.getVersion());
            }
            lVersionId = lNomenclatureVersions.iterator().next().getVersionId();
        }

        // Element Classifiable
        ElementClassifiable lElementClassifiable = new ElementClassifiable();
        lElementClassifiable.setCode(elementClassifiableIdDTO.getCodeEC());
        IElementClassifiableService lEcService = new ElementClassifiableServiceImpl();
        Set<String> lSetCodes = new HashSet<String>();
        lSetCodes.add(elementClassifiableIdDTO.getCodeEC());
        Collection<ElementClassifiable> lEcList = null;
        // TODO VERSION_ID
        if (lNomencalture.getIsPlate()) {
            lEcList = lEcService.findByCleFonctionnelle(GestionNomenclaturesConstantes.CODE_NOMENCLATURE_PLATE,
                    lVersionId, lSetCodes, null, null, null);
        } else {
            lEcList = lEcService.findByCleFonctionnelle(lCodeNomenclature, lVersionId, lSetCodes, null, null, null);
        }

        Collection<CodeExterne> lCodeExterneList = null;
        if (lEcList != null && !lEcList.isEmpty()) {
            CodeExterne lCodeExterne = new CodeExterne();
            ICodeExterneService lCodeExterneService = new CodeExterneServiceImpl();
            if (lNomencalture.getIsPlate()) {
                lCodeExterneList = lCodeExterneService.getCodeExterneList(lCodeExterne,
                        GestionNomenclaturesConstantes.CODE_NOMENCLATURE_PLATE, lSystemeCodage.getId(),
                        lEcList.iterator().next().getId(), GestionNomenclaturesConstantes.ETAT_ACTIF, 0);
            } else {
                lCodeExterneList = lCodeExterneService.getCodeExterneList(lCodeExterne, lCodeNomenclature,
                        lSystemeCodage.getId(), lEcList.iterator().next().getId(),
                        GestionNomenclaturesConstantes.ETAT_ACTIF, 0);
            }

        }
        if (lCodeExterneList != null) {
            return ElementClassifiableMapper.creatElementsClassifiablesFromCodesExternes(lCodeExterneList);
        }
        return null;
    }

    /**
     * @see com.sqli.sante.nomenclatures.service.ElementClassifiableManagerService#getECMetadonnees(java.util.List,
     *      com.sqli.sante.nomenclatures.dto.nomenclature.NomenclatureIdDTO)
     */
    @Override
    public Map<ElementClassifiableIdDTO, List<ECMetadonneeDTO>> getECMetadonnees(
            List<ElementClassifiableIdDTO> elementClassifiableIdDTO, NomenclatureIdDTO typeNomenclature)
            throws TechniqueException, UtilisationException {
        List<Long> listIdsEC = new ArrayList<Long>();
        List<ElementClassifiableIdDTO> idsARechercher = new ArrayList<ElementClassifiableIdDTO>();
        for (ElementClassifiableIdDTO idEC : elementClassifiableIdDTO) {
            if (idEC.getIdEc() != null) {
                listIdsEC.add(idEC.getIdEc());
            } else {
                idsARechercher.add(idEC);
            }
        }

        // map qui va servir a relier les elements passes en parametres a ceux recuperer plus bas
        Map<String, ElementClassifiableDTO> mapCodeElement = new HashMap<String, ElementClassifiableDTO>();
        if (!idsARechercher.isEmpty()) {
            // Certains elements doivent etre recherches via leur code
            ElementClassifiableRequestDTO request = new ElementClassifiableRequestDTO();
            request.setIdsElements(idsARechercher);
            request.setNomenclature(typeNomenclature);
            if (typeNomenclature.getVersion() == null) {
                Nomenclature lNomenclature = NomenclatureBO.checkECRequestDTO(request);
                GererElementClassifiableBO.getNomenclatureVersionId(request, lNomenclature);
            }
            Collection<ElementClassifiableDTO> searchElementClassifiable = searchElementClassifiable(request);
            if (searchElementClassifiable != null && !searchElementClassifiable.isEmpty()) {
                for (ElementClassifiableDTO elementClassifiableDTO : searchElementClassifiable) {
                    listIdsEC.add(elementClassifiableDTO.getElementClassifiableId().getIdEc());
                    mapCodeElement.put(elementClassifiableDTO.getCode(), elementClassifiableDTO);
                }
            }
        }
        Map<ElementClassifiableIdDTO, List<ECMetadonneeDTO>> mapIdElements = new HashMap<ElementClassifiableIdDTO, List<ECMetadonneeDTO>>();
        if (listIdsEC == null || listIdsEC.isEmpty()) {
            // on a pas trouve d'id d'elements correspondants donc on retourne la map vide
            return mapIdElements;
        }
        Map<Long, List<ECMetadonneeDTO>> ecMetadonnees = GererElementClassifiableBO.getECMetadonnees(listIdsEC,
                typeNomenclature.getCodeLibre());

        // une fois qu'on a recupere les metadonnees, on va les mapper avec les idElements passes en parametres
        for (ElementClassifiableIdDTO idDTO : elementClassifiableIdDTO) {
            if (idDTO.getIdEc() != null) {
                mapIdElements.put(idDTO, ecMetadonnees.get(idDTO.getIdEc()));
            } else {
                ElementClassifiableDTO elementClassifiableDTO = mapCodeElement.get(idDTO.getCodeEC());
                if (elementClassifiableDTO != null && elementClassifiableDTO.getElementClassifiableId() != null) {
                    mapIdElements.put(elementClassifiableDTO.getElementClassifiableId(),
                            ecMetadonnees.get(elementClassifiableDTO.getElementClassifiableId().getIdEc()));
                }
            }
        }
        return mapIdElements;
    }

    /*
     *
     * (non-Javadoc)
     * 
     * @see com.sqli.sante.nomenclatures.service.ElementClassifiableManagerService#searchTranscodage(com.sqli.sante.
     * nomenclatures.dto.transcodage.TranscodageRequestDTO)
     */
    @Override
    @Deprecated
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<ElementClassifiableDTO> searchTranscodage(TranscodageRequestDTO pTranscodageRequestDTO)
            throws TechniqueException, UtilisationException {
        NomenclatureUtil.checkTranscodageRequest(pTranscodageRequestDTO);
        if (pTranscodageRequestDTO.getModeNormal()) {
            return GererCodeExterneBO.searchCodeExterne(pTranscodageRequestDTO);
        } else {
            return GererElementClassifiableBO.searchElementsClassifiables(pTranscodageRequestDTO);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Map<ElementClassifiableIdDTO, Collection<ElementClassifiableDTO>> searchTranscodages(
            TranscodageRequestDTO pTranscodageRequestDTO) throws TechniqueException, UtilisationException {
        NomenclatureUtil.checkTranscodageRequest(pTranscodageRequestDTO);
        //
        pTranscodageRequestDTO
                .setElementsClassifiablesIdDTO(NomenclatureUtil.getElementsClassifiablesIdDTO(pTranscodageRequestDTO));
        //
        if (pTranscodageRequestDTO.isTranscodageInterNomenclature()) {
            TranscodageNomenclatureBO bo = new TranscodageNomenclatureBO();
            return bo.getTranscodageNomenclatureFromElements(pTranscodageRequestDTO);
        } else {
            if (pTranscodageRequestDTO.getModeNormal()) {
                return GererCodeExterneBO.searchCodeExterneByElementsClassifiables(pTranscodageRequestDTO);
            } else {
                return GererElementClassifiableBO.searchElementsClassifiablesByCodesExternes(pTranscodageRequestDTO);
            }
        }
    }

    /**
     * Récupérer les raccourcis des élements classifiables répondant au criteres spécifiés
     * 
     * @author aahadri
     *
     * @param requestDTO
     * @param libelleRecherche
     * @param typeNomenclature
     * @param versionId
     * @return
     * @throws TechniqueException
     * @throws UtilisationException
     */
    private static Collection<Raccourci> getRaccourcis(ComposantNomenclatureRequestDTO requestDTO,
            String libelleRecherche, String typeNomenclature, Long versionId)
            throws TechniqueException, UtilisationException {
        Collection<Raccourci> raccourcis = null;
        boolean inclureRaccourci = BooleanUtils.isTrue(requestDTO.getInclureRaccourci());
        if (inclureRaccourci) {
            IRaccourciService raccourciService = new RaccourciServiceImpl();
            long t = System.currentTimeMillis();
            boolean recherchelancee = false;
            Set<String> raccCodeList = NomenclatureUtil.filtreRaccourciCode(requestDTO.getIdsElements());
            // Cas ou la recherche porte sur un ou plusieurs code raccourci ==> récupérer les raccourcis ayant un de ces
            // codes elements
            if ((raccCodeList != null && !raccCodeList.isEmpty())
                    || BooleanUtils.isTrue(NomenclatureUtil.isRaccourciCode(requestDTO.getCode()))) {
                if (raccCodeList == null || raccCodeList.isEmpty()) {
                    raccCodeList = new HashSet<String>(1);
                    raccCodeList.add(requestDTO.getCode());
                }
                raccourcis = raccourciService.getRaccourcisByCodes(raccCodeList, typeNomenclature, null);
                recherchelancee = true;
            }
            // Cas ou la recherche integre les raccourcis sans spécification des codes raccourci concernée
            else {
                Raccourci raccourci = new Raccourci();
                raccourci.setLibelle(libelleRecherche);
                // Le code en question peut etre code d'element classifiable
                raccourci.setCodeElementClassifiable(requestDTO.getCode());
                // comme il peut etre code de raccourci
                raccourci.setCodeRaccourci(requestDTO.getCode());
                if (requestDTO.getActif() != null) {
                    raccourci.setEtat(BooleanUtils.isTrue(requestDTO.getActif())
                            ? GestionNomenclaturesConstantes.ETAT_ACTIF : GestionNomenclaturesConstantes.ETAT_INACTIF);
                }
                //
                // Envoi du versionId pour vérifier que les raccourcis retournées concernent des elements classifiables
                // faisont partie de cette versionId
                raccourcis = raccourciService.getRaccourcisForAvailableElementsVersion(raccourci,
                        requestDTO.getIncludeCode(), versionId, typeNomenclature, null);
                recherchelancee = true;
            }
            if (recherchelancee) {
                log.debug("-Nomenclature : Recherche des raccourcis en " + (System.currentTimeMillis() - t) + " ms");
                if (raccourcis != null) {
                    log.debug("-Nomenclature : " + raccourcis.size() + " Raccourci(s) trouvé(s)");
                } else {
                    log.debug("-Nomenclature : Aucun Raccourci trouvé");
                }
            }
        }
        return raccourcis;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Long saveUtilisation(FrequenceUtilisationDTO pvFrequenceUtilisation, String pvTypeNomenclature)
            throws UtilisationException, TechniqueException {

        FrequnceUtilisationServiceImpl lvFUSI = new FrequnceUtilisationServiceImpl();

        FrequenceUtilisation lvFU = new FrequenceUtilisation();
        lvFU.setCdContext(pvFrequenceUtilisation.getContext());
        lvFU.setCodeEc(pvFrequenceUtilisation.getCodeEC());
        lvFU.setDateUtil(new Date());
        lvFU.setIdUtilisateur(pvFrequenceUtilisation.getIdUtilisateur());
        lvFU.setVersion(pvFrequenceUtilisation.getVersion());

        return lvFUSI.save(lvFU, pvTypeNomenclature);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Collection<FrequenceUtilisationDTO> getUtilisation(FrequenceUtilisationRequestDTO pvRequestDTO)
            throws UtilisationException, TechniqueException {
        FrequnceUtilisationServiceImpl lvFUSI = new FrequnceUtilisationServiceImpl();

        return lvFUSI.find(pvRequestDTO, FrequnceUtilisationServiceImpl::notNullFilter);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Long saveHistoriquePatient(HistoriquePatientDTO pvHistoriquePatient, String pvTypeNomenclature)
            throws UtilisationException, TechniqueException {

        HistoriquePatientServiceImpl lvHPSI = new HistoriquePatientServiceImpl();

        HistoriquePatient lvHP = new HistoriquePatient();
        lvHP.setCdContext(pvHistoriquePatient.getContext());
        lvHP.setCodeEc(pvHistoriquePatient.getCodeEC());
        lvHP.setDateUtil(new Date());
        lvHP.setPatientId(pvHistoriquePatient.getPatientId());
        lvHP.setSejourId(pvHistoriquePatient.getSejourId());
        lvHP.setVersion(pvHistoriquePatient.getVersion());

        return lvHPSI.save(lvHP, pvTypeNomenclature);
    }

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Collection<HistoriquePatientDTO> getHistoriquePatient(HistoriquePatientRequestDTO pvRequestDTO)
			throws UtilisationException, TechniqueException {
		
		return new HistoriquePatientServiceImpl().find(pvRequestDTO, HistoriquePatientServiceImpl::notNullFilter);
	}
	
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public FavorisUserResponseDTO getFavorisUser(FavorisUserRequestDTO pvFavorisUserRequestDTO) 
			throws UtilisationException, TechniqueException {
		
		/** Get User's Root Favoris **/
		Optional<Collection<MesFavoris>> lvMesFavoris = FavoriUserBO.getUserFavoris(pvFavorisUserRequestDTO, Optional.of(value -> value.getDateDesactivation() == null));
		
		return FavoriUserBO.getUserFavorisDTO(lvMesFavoris.isPresent() ? lvMesFavoris.get() : new ArrayList<MesFavoris>(), 
												pvFavorisUserRequestDTO.getTypeNomenclature()
											  ).get();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Long saveFavorisElement(FavorisElementDTO pvFavorisElementDTO, ProfilUtilisateur pvProfilUtilisateur)
			throws UtilisationException, TechniqueException {
		
		return FavoriUserBO.saveECHasFavoris(pvFavorisElementDTO, pvProfilUtilisateur);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void deleteFavorisElements(Long pvFavorisId, Set<Long> pvFavorisElementIds, String pvNomenclature,  ProfilUtilisateur pvProfilUtilisateur)
			throws UtilisationException, TechniqueException {
		
		ElementClassifiableServiceManagerBO.deleteFavoisElements(pvFavorisId, pvFavorisElementIds, pvNomenclature, pvProfilUtilisateur);
	}
}
