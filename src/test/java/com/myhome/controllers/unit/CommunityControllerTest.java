/*
 * Copyright 2020 Prathab Murugan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myhome.controllers.unit;

import com.myhome.controllers.CommunityController;
import com.myhome.controllers.dto.CommunityAdminDto;
import com.myhome.controllers.dto.CommunityDto;
import com.myhome.controllers.dto.CommunityHouseDto;
import com.myhome.controllers.mapper.CommunityApiMapper;
import com.myhome.controllers.request.AddCommunityAdminRequest;
import com.myhome.controllers.request.AddCommunityHouseRequest;
import com.myhome.controllers.request.CreateCommunityRequest;
import com.myhome.controllers.response.AddCommunityAdminResponse;
import com.myhome.controllers.response.AddCommunityHouseResponse;
import com.myhome.controllers.response.CreateCommunityResponse;
import com.myhome.controllers.response.GetCommunityDetailsResponse;
import com.myhome.controllers.response.GetHouseDetailsResponse;
import com.myhome.controllers.response.ListCommunityAdminsResponse;
import com.myhome.domain.Community;
import com.myhome.domain.CommunityAdmin;
import com.myhome.domain.CommunityHouse;
import com.myhome.services.CommunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommunityControllerTest {
  private static final String COMMUNITY_ADMIN_ID = "1";
  private static final String COMMUNITY_HOUSE_ID = "2";
  private static final String COMMUNITY_HOUSE_NAME = "Test House";
  private static final String COMMUNITY_NAME = "Test Community";
  private static final String COMMUNITY_ID = "3";
  private static final String COMMUNITY_DISTRICT = "Wonderland";

  @Mock
  private CommunityService communityService;

  @Mock
  private CommunityApiMapper communityApiMapper;

  @InjectMocks
  private CommunityController communityController;

  @BeforeEach
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  private CommunityDto createTestCommunityDto() {
    Set<CommunityAdminDto> communityAdminDtos = new HashSet<>();
    communityAdminDtos.add(new CommunityAdminDto(COMMUNITY_ADMIN_ID));
    CommunityDto communityDto = new CommunityDto();
    communityDto.setCommunityId(COMMUNITY_ID);
    communityDto.setName(COMMUNITY_NAME);
    communityDto.setDistrict(COMMUNITY_DISTRICT);
    communityDto.setAdmins(communityAdminDtos);

    return communityDto;
  }

  private CommunityHouse createTestCommunityHouse(Community community) {
    return new CommunityHouse(community, COMMUNITY_HOUSE_NAME, COMMUNITY_HOUSE_ID, new HashSet<>());
  }

  private Community createTestCommunity() {
    Community community = new Community(new HashSet<>(), new HashSet<>(), COMMUNITY_NAME, COMMUNITY_ID, COMMUNITY_DISTRICT, new HashSet<>());
    CommunityAdmin admin = new CommunityAdmin(new HashSet<>(), COMMUNITY_ADMIN_ID);
    community.getAdmins().add(admin);
    community.getHouses().add(createTestCommunityHouse(community));
    admin.getCommunities().add(community);

    return community;
  }

  @Test
  void shouldCreateCommunitySuccessfully() {
    // given
    CreateCommunityRequest request =
        new CreateCommunityRequest(COMMUNITY_NAME, COMMUNITY_DISTRICT);
    CommunityDto communityDto = createTestCommunityDto();
    CreateCommunityResponse response =
        new CreateCommunityResponse(COMMUNITY_ID);
    Community community = createTestCommunity();

    given(communityApiMapper.createCommunityRequestToCommunityDto(request))
      .willReturn(communityDto);
    given(communityService.createCommunity(communityDto))
      .willReturn(community);
    given(communityApiMapper.communityToCreateCommunityResponse(community))
      .willReturn(response);

    // when
    ResponseEntity<CreateCommunityResponse> responseEntity =
      communityController.createCommunity(request);

    // then
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertEquals(response, responseEntity.getBody());
    verify(communityApiMapper).createCommunityRequestToCommunityDto(request);
    verify(communityApiMapper).communityToCreateCommunityResponse(community);
    verify(communityService).createCommunity(communityDto);
  }

  @Test
  void shouldListAllCommunitiesSuccessfully() {
    // given
    Set<Community> communities = new HashSet<>();
    Community community = createTestCommunity();
    communities.add(community);

    Set<GetCommunityDetailsResponse.Community> communityDetailsResponse
        = new HashSet<>();
    communityDetailsResponse.add(
      new GetCommunityDetailsResponse.Community(
          COMMUNITY_ID,
          COMMUNITY_NAME,
          COMMUNITY_DISTRICT
      )
    );

    GetCommunityDetailsResponse response = new GetCommunityDetailsResponse();
    response.getCommunities().addAll(communityDetailsResponse);

    Pageable pageable = PageRequest.of(0, 1);
    given(communityService.listAll(pageable))
      .willReturn(communities);
    given(communityApiMapper.communitySetToRestApiResponseCommunitySet(communities))
      .willReturn(communityDetailsResponse);

    // when
    ResponseEntity<GetCommunityDetailsResponse> responseEntity =
      communityController.listAllCommunity(pageable);

    // then
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(response, responseEntity.getBody());
    verify(communityApiMapper).communitySetToRestApiResponseCommunitySet(communities);
    verify(communityService).listAll(pageable);
  }

  @Test
  void shouldGetCommunityDetailsSuccessfully() {
    // given
    Optional<Community> communityOptional = Optional.of(createTestCommunity());
    Community community = communityOptional.get();
    GetCommunityDetailsResponse.Community communityDetails =
    new GetCommunityDetailsResponse.Community(
      COMMUNITY_ID,
      COMMUNITY_NAME,
      COMMUNITY_DISTRICT
    );

    Set<GetCommunityDetailsResponse.Community> communityDetailsResponse
      = new HashSet<>();
    communityDetailsResponse.add(communityDetails);

    GetCommunityDetailsResponse response = new GetCommunityDetailsResponse(communityDetailsResponse);

    given(communityService.getCommunityDetailsById(COMMUNITY_ID))
      .willReturn(communityOptional);
    given(communityApiMapper.communityToRestApiResponseCommunity(community))
      .willReturn(communityDetails);

    // when
    ResponseEntity<GetCommunityDetailsResponse> responseEntity =
      communityController.listCommunityDetails(COMMUNITY_ID);

    // then
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(response, responseEntity.getBody());
    verify(communityService).getCommunityDetailsById(COMMUNITY_ID);
    verify(communityApiMapper).communityToRestApiResponseCommunity(community);
  }

  @Test
  void shouldGetNotFoundListCommunityDetailsSuccess() {
    // given
    given(communityService.getCommunityDetailsById(COMMUNITY_ID))
      .willReturn(Optional.empty());

    // when
    ResponseEntity<GetCommunityDetailsResponse> responseEntity =
      communityController.listCommunityDetails(COMMUNITY_ID);

    // then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    assertNull(responseEntity.getBody());
    verify(communityService).getCommunityDetailsById(COMMUNITY_ID);
    verifyNoInteractions(communityApiMapper);
  }

  @Test
  void shouldListCommunityAdminsSuccess() {
    // given
    Community community = createTestCommunity();
    List<CommunityAdmin> admins = new ArrayList<>();
    admins.addAll(community.getAdmins());
    Optional<List<CommunityAdmin>> communityAdminsOptional = Optional.of(admins);

    Pageable pageable = PageRequest.of(0, 1);

    given(communityService.findCommunityAdminsById(COMMUNITY_ID, pageable))
      .willReturn(communityAdminsOptional);

    Set<CommunityAdmin> adminsSet = new HashSet<>(admins);

    Set<ListCommunityAdminsResponse.CommunityAdmin> listAdminsResponses = new HashSet<>();
    listAdminsResponses.add(
      new ListCommunityAdminsResponse.CommunityAdmin(
        COMMUNITY_ADMIN_ID
      )
    );

    given(communityApiMapper.communityAdminSetToRestApiResponseCommunityAdminSet(adminsSet))
      .willReturn(listAdminsResponses);

    ListCommunityAdminsResponse response = new ListCommunityAdminsResponse(listAdminsResponses);

    // when
    ResponseEntity<ListCommunityAdminsResponse> responseEntity =
      communityController.listCommunityAdmins(COMMUNITY_ID, pageable);

    // then
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(response, responseEntity.getBody());
    verify(communityApiMapper).communityAdminSetToRestApiResponseCommunityAdminSet(adminsSet);
    verify(communityService).findCommunityAdminsById(COMMUNITY_ID, pageable);
  }

  @Test
  void shouldReturnNoAdminDetailsNotFoundSuccess() {
    // given
    Pageable pageable = PageRequest.of(0, 1);

    given(communityService.findCommunityAdminsById(COMMUNITY_ID, pageable))
      .willReturn(Optional.empty());

    // when
    ResponseEntity<ListCommunityAdminsResponse> responseEntity =
      communityController.listCommunityAdmins(COMMUNITY_ID, pageable);

    // then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    assertNull(responseEntity.getBody());
    verify(communityService).findCommunityAdminsById(COMMUNITY_ID, pageable);
    verifyNoInteractions(communityApiMapper);
  }

  @Test
  void shouldAddCommunityAdminSuccess() {
    // given
    AddCommunityAdminRequest addRequest = new AddCommunityAdminRequest();
    Community community = createTestCommunity();
    Set<CommunityAdmin> communityAdmins = community.getAdmins();
    for (CommunityAdmin admin : communityAdmins) {
        addRequest.getAdmins().add(admin.getAdminId());
    }

    Set<String> adminIds = addRequest.getAdmins();
    AddCommunityAdminResponse response = new AddCommunityAdminResponse(adminIds);

    given(communityService.addAdminsToCommunity(COMMUNITY_ID, adminIds))
      .willReturn(Optional.of(community));

    // when
    ResponseEntity<AddCommunityAdminResponse> responseEntity =
      communityController.addCommunityAdmins(COMMUNITY_ID, addRequest);

    // then
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertEquals(response, responseEntity.getBody());
    verify(communityService).addAdminsToCommunity(COMMUNITY_ID, adminIds);
  }

  @Test
  void shouldNotAddAdminToCommunityNotFoundSuccessfully() {
    // given
    AddCommunityAdminRequest addRequest = new AddCommunityAdminRequest();
    Community community = createTestCommunity();
    Set<CommunityAdmin> communityAdmins = community.getAdmins();
    for (CommunityAdmin admin : communityAdmins) {
      addRequest.getAdmins().add(admin.getAdminId());
    }

    Set<String> adminIds = addRequest.getAdmins();

    given(communityService.addAdminsToCommunity(COMMUNITY_ID, adminIds))
      .willReturn(Optional.empty());

    // when
    ResponseEntity<AddCommunityAdminResponse> responseEntity =
      communityController.addCommunityAdmins(COMMUNITY_ID, addRequest);

    // then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    assertNull(responseEntity.getBody());
    verify(communityService).addAdminsToCommunity(COMMUNITY_ID, adminIds);
  }

  @Test
  void shouldListCommunityHousesSuccess() {
    Community community = createTestCommunity();
    List<CommunityHouse> houses = new ArrayList<>(community.getHouses());
    Set<CommunityHouse> housesSet = new HashSet<>(houses);
    Set<GetHouseDetailsResponse.CommunityHouse> getHouseDetailsSet = new HashSet<>();
    getHouseDetailsSet.add(new GetHouseDetailsResponse.CommunityHouse(
      COMMUNITY_HOUSE_ID,
      COMMUNITY_HOUSE_NAME
    ));

    GetHouseDetailsResponse response = new GetHouseDetailsResponse(getHouseDetailsSet);
    Pageable pageable = PageRequest.of(0, 1);

    given(communityService.findCommunityHousesById(COMMUNITY_ID, pageable))
      .willReturn(Optional.of(houses));
    given(communityApiMapper.communityHouseSetToRestApiResponseCommunityHouseSet(housesSet))
      .willReturn(getHouseDetailsSet);

    // when
    ResponseEntity<GetHouseDetailsResponse> responseEntity =
      communityController.listCommunityHouses(COMMUNITY_ID, pageable);

    //then
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(response, responseEntity.getBody());
    verify(communityService).findCommunityHousesById(COMMUNITY_ID, pageable);
    verify(communityApiMapper).communityHouseSetToRestApiResponseCommunityHouseSet(housesSet);
  }

  @Test
  void testListCommunityHousesCommunityNotExistSuccess() {
    // given
    Pageable pageable = PageRequest.of(0, 1);
    given(communityService.findCommunityHousesById(COMMUNITY_ID, pageable))
      .willReturn(Optional.empty());

    // when
    ResponseEntity<GetHouseDetailsResponse> responseEntity =
      communityController.listCommunityHouses(COMMUNITY_ID, pageable);

    // then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    assertNull(responseEntity.getBody());
    verify(communityService).findCommunityHousesById(COMMUNITY_ID, pageable);
    verifyNoInteractions(communityApiMapper);
  }

  @Test
  void shouldAddCommunityHouseSuccessfully() {
    // given
    AddCommunityHouseRequest addCommunityHouseRequest = new AddCommunityHouseRequest();
    Community community = createTestCommunity();
    Set<CommunityHouse> communityHouses = community.getHouses();
    Set<CommunityHouseDto> communityHouseDtos = new HashSet<>();
    communityHouseDtos.add(new CommunityHouseDto(COMMUNITY_HOUSE_ID, COMMUNITY_HOUSE_NAME));

    Set<String> houseIds = new HashSet<>();
    for (CommunityHouse house : communityHouses) {
      houseIds.add(house.getHouseId());
    }

    addCommunityHouseRequest.getHouses().addAll(communityHouseDtos);

    AddCommunityHouseResponse response = new AddCommunityHouseResponse(houseIds);

    given(communityApiMapper.communityHouseDtoSetToCommunityHouseSet(communityHouseDtos))
      .willReturn(communityHouses);
    given(communityService.addHousesToCommunity(COMMUNITY_ID, communityHouses))
      .willReturn(houseIds);

    // when
    ResponseEntity<AddCommunityHouseResponse> responseEntity =
      communityController.addCommunityHouses(COMMUNITY_ID, addCommunityHouseRequest);

    // then
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertEquals(response, responseEntity.getBody());
    verify(communityApiMapper).communityHouseDtoSetToCommunityHouseSet(communityHouseDtos);
    verify(communityService).addHousesToCommunity(COMMUNITY_ID, communityHouses);
  }

  @Test
  void shouldThrowBadRequestWithEmptyAddHouseRequest() {
    // given
    AddCommunityHouseRequest emptyRequest = new AddCommunityHouseRequest();

    given(communityApiMapper.communityHouseDtoSetToCommunityHouseSet(emptyRequest.getHouses()))
      .willReturn(new HashSet<>());
    given(communityService.addHousesToCommunity(COMMUNITY_ID, new HashSet<>()))
      .willReturn(new HashSet<>());

    // when
    ResponseEntity<AddCommunityHouseResponse> responseEntity =
      communityController.addCommunityHouses(COMMUNITY_ID, emptyRequest);

    // then
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    assertNull(responseEntity.getBody());
    verify(communityApiMapper).communityHouseDtoSetToCommunityHouseSet(new HashSet<>());
    verify(communityService).addHousesToCommunity(COMMUNITY_ID, new HashSet<>());
  }

  @Test
  void shouldRemoveCommunityHouseSuccessfully() {
    // given
    given(communityService.removeHouseFromCommunityByHouseId(COMMUNITY_ID, COMMUNITY_HOUSE_ID))
      .willReturn(true);

    // when
    ResponseEntity<Void> responseEntity =
      communityController.removeCommunityHouse(COMMUNITY_ID, COMMUNITY_HOUSE_ID);

    // then
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    verify(communityService).removeHouseFromCommunityByHouseId(COMMUNITY_ID, COMMUNITY_HOUSE_ID);
  }

  @Test
  void shouldNotRemoveCommunityHouseIfNotFoundSuccessfully() {
    // given
    given(communityService.removeHouseFromCommunityByHouseId(COMMUNITY_ID, COMMUNITY_HOUSE_ID))
      .willReturn(false);

    // when
    ResponseEntity<Void> responseEntity =
      communityController.removeCommunityHouse(COMMUNITY_ID, COMMUNITY_HOUSE_ID);

    // then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    verify(communityService).removeHouseFromCommunityByHouseId(COMMUNITY_ID, COMMUNITY_HOUSE_ID);
  }

  @Test
  void shouldRemoveAdminFromCommunitySuccessfully() {
    // given
    given(communityService.removeAdminFromCommunity(COMMUNITY_ID, COMMUNITY_ADMIN_ID))
      .willReturn(true);

    // when
    ResponseEntity<Void> responseEntity =
      communityController.removeAdminFromCommunity(COMMUNITY_ID, COMMUNITY_ADMIN_ID);

    // then
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    verify(communityService).removeAdminFromCommunity(COMMUNITY_ID, COMMUNITY_ADMIN_ID);
  }

  @Test
  void shouldNotRemoveAdminIfNotFoundSuccessfully() {
    // given
    given(communityService.removeAdminFromCommunity(COMMUNITY_ID, COMMUNITY_ADMIN_ID))
      .willReturn(false);

    // when
    ResponseEntity<Void> responseEntity =
      communityController.removeAdminFromCommunity(COMMUNITY_ID, COMMUNITY_ADMIN_ID);

    // then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    verify(communityService).removeAdminFromCommunity(COMMUNITY_ID, COMMUNITY_ADMIN_ID);
  }

  @Test
  void shouldDeleteCommunitySuccessfully() {
    // given
    given(communityService.deleteCommunity(COMMUNITY_ID))
      .willReturn(true);

    // when
    ResponseEntity<Void> responseEntity =
      communityController.deleteCommunity(COMMUNITY_ID);

    // then
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    verify(communityService).deleteCommunity(COMMUNITY_ID);
  }

  @Test
  void shouldNotDeleteCommunityNotFoundSuccessfully() {
    // given
    given(communityService.deleteCommunity(COMMUNITY_ID))
      .willReturn(false);

    // when
    ResponseEntity<Void> responseEntity =
      communityController.deleteCommunity(COMMUNITY_ID);

    // then
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    verify(communityService).deleteCommunity(COMMUNITY_ID);
  }
}