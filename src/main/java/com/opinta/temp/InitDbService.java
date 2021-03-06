package com.opinta.temp;

import com.opinta.dao.ParcelItemDao;
import com.opinta.dto.PostOfficeDto;
import com.opinta.dto.ShipmentDto;
import com.opinta.entity.Counterparty;
import com.opinta.entity.Shipment;
import com.opinta.entity.Parcel;
import com.opinta.entity.ParcelItem;
import com.opinta.entity.Client;
import com.opinta.entity.DeliveryType;
import com.opinta.entity.PostOffice;
import com.opinta.entity.ShipmentStatus;
import com.opinta.entity.ShipmentTrackingDetail;
import com.opinta.entity.TariffGrid;
import com.opinta.entity.Address;
import com.opinta.entity.W2wVariation;
import com.opinta.entity.BarcodeInnerNumber;
import com.opinta.entity.PostcodePool;
import com.opinta.mapper.ShipmentTrackingDetailMapper;
import com.opinta.service.ShipmentTrackingDetailService;
import com.opinta.service.TariffGridService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import com.opinta.dto.AddressDto;
import com.opinta.dto.BarcodeInnerNumberDto;
import com.opinta.dto.PostcodePoolDto;
import com.opinta.dto.CounterpartyDto;
import com.opinta.mapper.AddressMapper;
import com.opinta.mapper.BarcodeInnerNumberMapper;
import com.opinta.mapper.ClientMapper;
import com.opinta.mapper.PostOfficeMapper;
import com.opinta.mapper.PostcodePoolMapper;
import com.opinta.mapper.ShipmentMapper;
import com.opinta.mapper.CounterpartyMapper;
import com.opinta.service.AddressService;
import com.opinta.service.BarcodeInnerNumberService;
import com.opinta.service.ClientService;
import com.opinta.service.PostOfficeService;
import com.opinta.service.PostcodePoolService;
import com.opinta.service.ShipmentService;
import com.opinta.service.CounterpartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.opinta.entity.BarcodeStatus.RESERVED;
import static com.opinta.entity.BarcodeStatus.USED;

@Service
public class InitDbService {
    private BarcodeInnerNumberService barcodeInnerNumberService;
    private PostcodePoolService postcodePoolService;
    private ClientService clientService;
    private AddressService addressService;
    private ShipmentService shipmentService;
    private CounterpartyService counterpartyService;
    private PostOfficeService postOfficeService;
    private ShipmentTrackingDetailService shipmentTrackingDetailService;
    private TariffGridService tariffGridService;

    private ClientMapper clientMapper;
    private AddressMapper addressMapper;
    private PostcodePoolMapper postcodePoolMapper;
    private BarcodeInnerNumberMapper barcodeInnerNumberMapper;
    private ShipmentMapper shipmentMapper;
    private PostOfficeMapper postOfficeMapper;
    private CounterpartyMapper counterpartyMapper;
    private ShipmentTrackingDetailMapper shipmentTrackingDetailMapper;

    //private ParcelItemDao parcelItemDao;

    @Autowired
    public InitDbService(
            BarcodeInnerNumberService barcodeInnerNumberService, PostcodePoolService postcodePoolService,
            ClientService clientService, AddressService addressService, ShipmentService shipmentService,
            CounterpartyService counterpartyService, PostOfficeService postOfficeService,
            ShipmentTrackingDetailService shipmentTrackingDetailService, TariffGridService tariffGridService,
            ClientMapper clientMapper, AddressMapper addressMapper, PostcodePoolMapper postcodePoolMapper,
            BarcodeInnerNumberMapper barcodeInnerNumberMapper, ShipmentMapper shipmentMapper,
            PostOfficeMapper postOfficeMapper, CounterpartyMapper counterpartyMapper,
            ShipmentTrackingDetailMapper shipmentTrackingDetailMapper,
            ParcelItemDao parcelItemDao) {
        this.barcodeInnerNumberService = barcodeInnerNumberService;
        this.postcodePoolService = postcodePoolService;
        this.clientService = clientService;
        this.addressService = addressService;
        this.shipmentService = shipmentService;
        this.counterpartyService = counterpartyService;
        this.postOfficeService = postOfficeService;
        this.shipmentTrackingDetailService = shipmentTrackingDetailService;
        this.tariffGridService = tariffGridService;
        this.clientMapper = clientMapper;
        this.addressMapper = addressMapper;
        this.postcodePoolMapper = postcodePoolMapper;
        this.barcodeInnerNumberMapper = barcodeInnerNumberMapper;
        this.shipmentMapper = shipmentMapper;
        this.postOfficeMapper = postOfficeMapper;
        this.counterpartyMapper = counterpartyMapper;
        this.shipmentTrackingDetailMapper = shipmentTrackingDetailMapper;
        //this.parcelItemDao = parcelItemDao;
    }

    @PostConstruct
    public void init() {
        populateDb();
    }

    private void populateDb() {
        // populate TariffGrid
        populateTariffGrid();

        // create PostcodePool with BarcodeInnerNumber
        PostcodePoolDto postcodePoolDto = postcodePoolMapper.toDto(new PostcodePool("00001", false));
        final long postcodePoolId = postcodePoolService.save(postcodePoolDto)
                .getId();

        List<BarcodeInnerNumberDto> barcodeInnerNumbers = new ArrayList<>();
        barcodeInnerNumbers.add(barcodeInnerNumberMapper.toDto(new BarcodeInnerNumber("0000001", USED)));
        barcodeInnerNumbers.add(barcodeInnerNumberMapper.toDto(new BarcodeInnerNumber("0000002", RESERVED)));
        barcodeInnerNumbers.add(barcodeInnerNumberMapper.toDto(new BarcodeInnerNumber("0000003", RESERVED)));

        postcodePoolService.addBarcodeInnerNumbers(postcodePoolId, barcodeInnerNumbers);

        // create Address
        List<AddressDto> addresses = new ArrayList<>();
        List<AddressDto> addressesSaved = new ArrayList<>();
        addresses.add(addressMapper.toDto(new Address("00001", "Ternopil",
                "Monastiriska", "Monastiriska",
                "Sadova", "51", "")));
        addresses.add(addressMapper.toDto(new Address("00002", "Kiev", "", "Kiev", "Khreschatik", "121", "37")));
        addresses.forEach((AddressDto addressDto) -> addressesSaved.add(addressService.save(addressDto)));

        // create Client with Counterparty
        PostcodePoolDto postcodePoolDto1 = postcodePoolMapper.toDto(new PostcodePool("00003", false));
        PostcodePoolDto postcodePoolDtoSaved1 = postcodePoolService.save(postcodePoolDto1);
        Counterparty counterparty = new Counterparty("Modna kasta",
                postcodePoolMapper.toEntity(postcodePoolDtoSaved1));
        CounterpartyDto counterpartyDto = this.counterpartyMapper.toDto(counterparty);
        counterpartyDto = counterpartyService.save(counterpartyDto);
        counterparty = counterpartyMapper.toEntity(counterpartyDto);
        List<Client> clients = new ArrayList<>();
        List<Client> clientsSaved = new ArrayList<>();
        clients.add(new Client("FOP Ivanov", "001",
                addressMapper.toEntity(addressesSaved.get(0)), counterparty));
        clients.add(new Client("Petrov PP", "002",
                addressMapper.toEntity(addressesSaved.get(1)), counterparty));
        clients.forEach((Client client) ->
            clientsSaved.add(this.clientMapper.toEntity(clientService.save(this.clientMapper.toDto(client))))
        );

        //create ParcelsItem

        List<ParcelItem> parcelItemsForSave = new ArrayList<>();
        parcelItemsForSave.add(new ParcelItem("Some item", 1, 2.0F, new BigDecimal("10.5")));
        parcelItemsForSave.add(new ParcelItem("Some other item", 2, 3.0F, new BigDecimal("20.5")));

        parcelItemsForSave.add(new ParcelItem("Some other item", 1, 4.0F, new BigDecimal("20.5")));
        parcelItemsForSave.add(new ParcelItem("Some other item", 2, 5.0F, new BigDecimal("25.5")));

        parcelItemsForSave.add(new ParcelItem("Some other item", 6, 6.5F, new BigDecimal("30.5")));
        parcelItemsForSave.add(new ParcelItem("Some other item", 4, 7.8F, new BigDecimal("27.5")));

        parcelItemsForSave.add(new ParcelItem("Some other item", 1, 2.5F, new BigDecimal("10.5")));
        parcelItemsForSave.add(new ParcelItem("Some other item", 3, 2.8F, new BigDecimal("23.5")));

        parcelItemsForSave.add(new ParcelItem("Some other item", 2, 3.5F, new BigDecimal("33.5")));
        parcelItemsForSave.add(new ParcelItem("Some other item", 4, 5.8F, new BigDecimal("22.5")));

        parcelItemsForSave.add(new ParcelItem("Some other item", 2, 3.5F, new BigDecimal("33.5")));
        parcelItemsForSave.add(new ParcelItem("Some other item", 4, 5.8F, new BigDecimal("22.5")));

        //create Parcels

        List<Parcel> parcelsForSave = new ArrayList<>();
        parcelsForSave.add(new Parcel(parcelItemsForSave.subList(0,2), 3F, 1F, 3F, 3F,
                new BigDecimal("8.5"), new BigDecimal("2.25")));
        parcelsForSave.add(new Parcel(parcelItemsForSave.subList(2,4), 3F, 3F, 3F, 3F,
                new BigDecimal("7.5"), new BigDecimal("2.25")));

        parcelsForSave.add(new Parcel(parcelItemsForSave.subList(4,6), 3F, 3F, 5F, 3F,
                new BigDecimal("4.5"), new BigDecimal("2.25")));
        parcelsForSave.add(new Parcel(parcelItemsForSave.subList(6,8), 3F, 3F, 3F, 6F,
                new BigDecimal("6.5"), new BigDecimal("2.25")));

        parcelsForSave.add(new Parcel(parcelItemsForSave.subList(8,10), 2F, 3F, 7F, 3F,
                new BigDecimal("1.5"), new BigDecimal("2.25")));
        parcelsForSave.add(new Parcel(parcelItemsForSave.subList(10,12), 3F, 6F, 6F, 3F,
                new BigDecimal("9.5"), new BigDecimal("2.25")));

        // create Shipment
        List<ShipmentDto> shipmentsSaved = new ArrayList<>();
        Shipment shipment = new Shipment(clientsSaved.get(0), clientsSaved.get(1), DeliveryType.W2W,
                new BigDecimal("15"), parcelsForSave.subList(0, 2));
        shipmentsSaved.add(shipmentService.save(shipmentMapper.toDto(shipment)));
        shipment = new Shipment(clientsSaved.get(0), clientsSaved.get(0), DeliveryType.W2D,
                 new BigDecimal("20.5"), parcelsForSave.subList(2, 4));
        shipmentsSaved.add(shipmentService.save(shipmentMapper.toDto(shipment)));
        shipment = new Shipment(clientsSaved.get(1), clientsSaved.get(0), DeliveryType.D2D,
                new BigDecimal("13.5"), parcelsForSave.subList(4, 6));
        shipmentsSaved.add(shipmentService.save(shipmentMapper.toDto(shipment)));

        // create PostOffice
        PostcodePoolDto postcodePoolDto2 = postcodePoolMapper.toDto(new PostcodePool("00002", false));
        PostcodePoolDto postcodePoolDtoSaved = postcodePoolService.save(postcodePoolDto2);
        PostOffice postOffice = new PostOffice("Lviv post office", addressMapper.toEntity(addressesSaved.get(0)),
                postcodePoolMapper.toEntity(postcodePoolDtoSaved));
        PostOfficeDto postOfficeSaved = postOfficeService.save(postOfficeMapper.toDto(postOffice));

        // create ShipmentTrackingDetail
        ShipmentTrackingDetail shipmentTrackingDetail =
                new ShipmentTrackingDetail(shipmentMapper.toEntity(shipmentsSaved.get(0)),
                        postOfficeMapper.toEntity(postOfficeSaved), ShipmentStatus.PREPARED, new Date());
        shipmentTrackingDetailService.save(shipmentTrackingDetailMapper.toDto(shipmentTrackingDetail));
    }

    private void populateTariffGrid() {
        List<TariffGrid> tariffGrids = new ArrayList<>();

        tariffGrids.add(new TariffGrid(0.25f, 30f, W2wVariation.TOWN, 12f));
        tariffGrids.add(new TariffGrid(0.25f, 30f, W2wVariation.REGION, 15f));
        tariffGrids.add(new TariffGrid(0.25f, 30f, W2wVariation.COUNTRY, 21f));

        tariffGrids.add(new TariffGrid(0.5f, 30f, W2wVariation.TOWN, 15f));
        tariffGrids.add(new TariffGrid(0.5f, 30f, W2wVariation.REGION, 18f));
        tariffGrids.add(new TariffGrid(0.5f, 30f, W2wVariation.COUNTRY, 24f));

        tariffGrids.add(new TariffGrid(1f, 30f, W2wVariation.TOWN, 18f));
        tariffGrids.add(new TariffGrid(1f, 30f, W2wVariation.REGION, 21f));
        tariffGrids.add(new TariffGrid(1f, 30f, W2wVariation.COUNTRY, 27f));

        tariffGrids.add(new TariffGrid(2f, 30f, W2wVariation.TOWN, 21f));
        tariffGrids.add(new TariffGrid(2f, 30f, W2wVariation.REGION, 24f));
        tariffGrids.add(new TariffGrid(2f, 30f, W2wVariation.COUNTRY, 30f));

        tariffGrids.add(new TariffGrid(5f, 70f, W2wVariation.TOWN, 24f));
        tariffGrids.add(new TariffGrid(5f, 70f, W2wVariation.REGION, 27f));
        tariffGrids.add(new TariffGrid(5f, 70f, W2wVariation.COUNTRY, 36f));

        tariffGrids.add(new TariffGrid(10f, 70f, W2wVariation.TOWN, 27f));
        tariffGrids.add(new TariffGrid(10f, 70f, W2wVariation.REGION, 30f));
        tariffGrids.add(new TariffGrid(10f, 70f, W2wVariation.COUNTRY, 42f));

        tariffGrids.add(new TariffGrid(15f, 70f, W2wVariation.TOWN, 30f));
        tariffGrids.add(new TariffGrid(15f, 70f, W2wVariation.REGION, 36f));
        tariffGrids.add(new TariffGrid(15f, 70f, W2wVariation.COUNTRY, 48f));

        tariffGrids.add(new TariffGrid(20f, 70f, W2wVariation.TOWN, 36f));
        tariffGrids.add(new TariffGrid(20f, 70f, W2wVariation.REGION, 42f));
        tariffGrids.add(new TariffGrid(20f, 70f, W2wVariation.COUNTRY, 54f));

        tariffGrids.add(new TariffGrid(30f, 70f, W2wVariation.TOWN, 42f));
        tariffGrids.add(new TariffGrid(30f, 70f, W2wVariation.REGION, 48f));
        tariffGrids.add(new TariffGrid(30f, 70f, W2wVariation.COUNTRY, 60f));

        tariffGrids.forEach(tariffGridService::save);
    }
}
