package org.onvif.ver10.search.wsdl;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 3.5.5
 * Generated source version: 3.5.5
 *
 */
@WebService(targetNamespace = "http://www.onvif.org/ver10/search/wsdl", name = "SearchPort")
@XmlSeeAlso({org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.w3._2004._08.xop.include.ObjectFactory.class, org.onvif.ver10.schema.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class, ObjectFactory.class, org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.w3._2003._05.soap_envelope.ObjectFactory.class, org.w3._2005._05.xmlmime.ObjectFactory.class})
public interface SearchPort {

    /**
     * Returns the capabilities of the search service. The result is returned in a typed answer.
     */
    @WebMethod(operationName = "GetServiceCapabilities", action = "http://www.onvif.org/ver10/search/wsdl/GetServiceCapabilities")
    @RequestWrapper(localName = "GetServiceCapabilities", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetServiceCapabilities")
    @ResponseWrapper(localName = "GetServiceCapabilitiesResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetServiceCapabilitiesResponse")
    @WebResult(name = "Capabilities", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public org.onvif.ver10.search.wsdl.Capabilities getServiceCapabilities()
;

    /**
     * FindPTZPosition starts a search session, looking for ptz positions in the scope (See 20.2.4)
     * 				that matches the search filter defined in the request. Results from the search are acquired
     * 				using the GetPTZPositionSearchResults request, specifying the search token returned from
     * 				this request.
     * 				The device shall continue searching until one of the following occurs:
     * 				This operation is mandatory to support whenever CanContainPTZ is true for any metadata
     * 				track in any recording on the device.
     */
    @WebMethod(operationName = "FindPTZPosition", action = "http://www.onvif.org/ver10/search/wsdl/FindPTZPosition")
    @RequestWrapper(localName = "FindPTZPosition", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.FindPTZPosition")
    @ResponseWrapper(localName = "FindPTZPositionResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.FindPTZPositionResponse")
    @WebResult(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public java.lang.String findPTZPosition(

        @WebParam(name = "StartPoint", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.XMLGregorianCalendar startPoint,
        @WebParam(name = "EndPoint", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.XMLGregorianCalendar endPoint,
        @WebParam(name = "Scope", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        org.onvif.ver10.schema.SearchScope scope,
        @WebParam(name = "SearchFilter", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        org.onvif.ver10.schema.PTZPositionFilter searchFilter,
        @WebParam(name = "MaxMatches", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer maxMatches,
        @WebParam(name = "KeepAliveTime", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.Duration keepAliveTime
    );

    /**
     * FindEvents starts a search session, looking for recording events (in the scope that
     * 				matches the search filter defined in the request. Results from the search are
     * 				acquired using the GetEventSearchResults request, specifying the search token returned from
     * 				this request.
     * 				The device shall continue searching until one of the following occurs:
     * 				Results shall be ordered by time, ascending in case of forward search, or descending in case
     * 				of backward search. This operation is mandatory to support for a device implementing the
     * 				recording search service.
     */
    @WebMethod(operationName = "FindEvents", action = "http://www.onvif.org/ver10/search/wsdl/FindEvents")
    @RequestWrapper(localName = "FindEvents", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.FindEvents")
    @ResponseWrapper(localName = "FindEventsResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.FindEventsResponse")
    @WebResult(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public java.lang.String findEvents(

        @WebParam(name = "StartPoint", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.XMLGregorianCalendar startPoint,
        @WebParam(name = "EndPoint", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.XMLGregorianCalendar endPoint,
        @WebParam(name = "Scope", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        org.onvif.ver10.schema.SearchScope scope,
        @WebParam(name = "SearchFilter", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        org.onvif.ver10.schema.EventFilter searchFilter,
        @WebParam(name = "IncludeStartState", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        boolean includeStartState,
        @WebParam(name = "MaxMatches", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer maxMatches,
        @WebParam(name = "KeepAliveTime", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.Duration keepAliveTime
    );

    /**
     * GetRecordingSearchResults acquires the results from a recording search session previously
     * 				initiated by a FindRecordings operation. The response shall not include results already
     * 				returned in previous requests for the same session. If MaxResults is specified, the response
     * 				shall not contain more than MaxResults results. The number of results relates to the number of recordings.
     * 				For viewing individual recorded data for a signal track use the FindEvents method.
     * 				GetRecordingSearchResults shall block until:
     * 				This operation is mandatory to support for a device implementing the recording search service.
     */
    @WebMethod(operationName = "GetRecordingSearchResults", action = "http://www.onvif.org/ver10/search/wsdl/GetRecordingSearchResults")
    @RequestWrapper(localName = "GetRecordingSearchResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetRecordingSearchResults")
    @ResponseWrapper(localName = "GetRecordingSearchResultsResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetRecordingSearchResultsResponse")
    @WebResult(name = "ResultList", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public org.onvif.ver10.schema.FindRecordingResultList getRecordingSearchResults(

        @WebParam(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.String searchToken,
        @WebParam(name = "MinResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer minResults,
        @WebParam(name = "MaxResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer maxResults,
        @WebParam(name = "WaitTime", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.Duration waitTime
    );

    /**
     * GetRecordingSummary is used to get a summary description of all recorded data. This
     * 				operation is mandatory to support for a device implementing the recording search service.
     */
    @WebMethod(operationName = "GetRecordingSummary", action = "http://www.onvif.org/ver10/search/wsdl/GetRecordingSummary")
    @RequestWrapper(localName = "GetRecordingSummary", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetRecordingSummary")
    @ResponseWrapper(localName = "GetRecordingSummaryResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetRecordingSummaryResponse")
    @WebResult(name = "Summary", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public org.onvif.ver10.schema.RecordingSummary getRecordingSummary()
;

    /**
     * GetPTZPositionSearchResults acquires the results from a ptz position search session
     * 				previously initiated by a FindPTZPosition operation. The response shall not include results
     * 				already returned in previous requests for the same session. If MaxResults is specified, the
     * 				response shall not contain more than MaxResults results.
     * 				GetPTZPositionSearchResults shall block until:
     * 				This operation is mandatory to support whenever CanContainPTZ is true for any metadata
     * 				track in any recording on the device.
     */
    @WebMethod(operationName = "GetPTZPositionSearchResults", action = "http://www.onvif.org/ver10/search/wsdl/GetPTZPositionSearchResults")
    @RequestWrapper(localName = "GetPTZPositionSearchResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetPTZPositionSearchResults")
    @ResponseWrapper(localName = "GetPTZPositionSearchResultsResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetPTZPositionSearchResultsResponse")
    @WebResult(name = "ResultList", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public org.onvif.ver10.schema.FindPTZPositionResultList getPTZPositionSearchResults(

        @WebParam(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.String searchToken,
        @WebParam(name = "MinResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer minResults,
        @WebParam(name = "MaxResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer maxResults,
        @WebParam(name = "WaitTime", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.Duration waitTime
    );

    /**
     * GetSearchState returns the current state of the specified search session. This command is deprecated .
     */
    @WebMethod(operationName = "GetSearchState", action = "http://www.onvif.org/ver10/search/wsdl/GetSearchState")
    @RequestWrapper(localName = "GetSearchState", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetSearchState")
    @ResponseWrapper(localName = "GetSearchStateResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetSearchStateResponse")
    @WebResult(name = "State", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public org.onvif.ver10.schema.SearchState getSearchState(

        @WebParam(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.String searchToken
    );

    /**
     * FindMetadata starts a search session, looking for metadata in the scope (See 20.2.4) that
     * 				matches the search filter defined in the request. Results from the search are acquired using
     * 				the GetMetadataSearchResults request, specifying the search token returned from this
     * 				request.
     * 				The device shall continue searching until one of the following occurs:
     * 				This operation is mandatory to support if the MetaDataSearch capability is set to true in the
     * 				SearchCapabilities structure return by the GetCapabilities command in the Device service.
     */
    @WebMethod(operationName = "FindMetadata", action = "http://www.onvif.org/ver10/search/wsdl/FindMetadata")
    @RequestWrapper(localName = "FindMetadata", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.FindMetadata")
    @ResponseWrapper(localName = "FindMetadataResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.FindMetadataResponse")
    @WebResult(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public java.lang.String findMetadata(

        @WebParam(name = "StartPoint", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.XMLGregorianCalendar startPoint,
        @WebParam(name = "EndPoint", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.XMLGregorianCalendar endPoint,
        @WebParam(name = "Scope", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        org.onvif.ver10.schema.SearchScope scope,
        @WebParam(name = "MetadataFilter", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        org.onvif.ver10.schema.MetadataFilter metadataFilter,
        @WebParam(name = "MaxMatches", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer maxMatches,
        @WebParam(name = "KeepAliveTime", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.Duration keepAliveTime
    );

    /**
     * GetEventSearchResults acquires the results from a recording event search session previously
     * 				initiated by a FindEvents operation. The response shall not include results already returned in
     * 				previous requests for the same session. If MaxResults is specified, the response shall not
     * 				contain more than MaxResults results.
     * 				GetEventSearchResults shall block until:
     * 				This operation is mandatory to support for a device implementing the recording search service.
     */
    @WebMethod(operationName = "GetEventSearchResults", action = "http://www.onvif.org/ver10/search/wsdl/GetEventSearchResults")
    @RequestWrapper(localName = "GetEventSearchResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetEventSearchResults")
    @ResponseWrapper(localName = "GetEventSearchResultsResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetEventSearchResultsResponse")
    @WebResult(name = "ResultList", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public org.onvif.ver10.schema.FindEventResultList getEventSearchResults(

        @WebParam(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.String searchToken,
        @WebParam(name = "MinResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer minResults,
        @WebParam(name = "MaxResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer maxResults,
        @WebParam(name = "WaitTime", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.Duration waitTime
    );

    /**
     * EndSearch stops and ongoing search session, causing any blocking result request to return
     * 				and the SearchToken to become invalid. If the search was interrupted before completion, the
     * 				point in time that the search had reached shall be returned. If the search had not yet begun,
     * 				the StartPoint shall be returned. If the search was completed the original EndPoint supplied
     * 				by the Find operation shall be returned. When issuing EndSearch on a FindRecordings request the
     * 				EndPoint is undefined and shall not be used since the FindRecordings request doesn't have
     * 				StartPoint/EndPoint. This operation is mandatory to support for a device implementing the
     * 				recording search service.
     * 			
     */
    @WebMethod(operationName = "EndSearch", action = "http://www.onvif.org/ver10/search/wsdl/EndSearch")
    @RequestWrapper(localName = "EndSearch", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.EndSearch")
    @ResponseWrapper(localName = "EndSearchResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.EndSearchResponse")
    @WebResult(name = "Endpoint", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public javax.xml.datatype.XMLGregorianCalendar endSearch(

        @WebParam(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.String searchToken
    );

    /**
     * Returns a set of media attributes for all tracks of the specified recordings at a specified point
     * 				in time. Clients using this operation shall be able to use it as a non blocking operation. A
     * 				device shall set the starttime and endtime of the MediaAttributes structure to equal values if
     * 				calculating this range would causes this operation to block. See MediaAttributes structure for
     * 				more information. This operation is mandatory to support for a device implementing the
     * 				recording search service.
     */
    @WebMethod(operationName = "GetMediaAttributes", action = "http://www.onvif.org/ver10/search/wsdl/GetMediaAttributes")
    @RequestWrapper(localName = "GetMediaAttributes", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetMediaAttributes")
    @ResponseWrapper(localName = "GetMediaAttributesResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetMediaAttributesResponse")
    @WebResult(name = "MediaAttributes", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public java.util.List<org.onvif.ver10.schema.MediaAttributes> getMediaAttributes(

        @WebParam(name = "RecordingTokens", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.util.List<java.lang.String> recordingTokens,
        @WebParam(name = "Time", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.XMLGregorianCalendar time
    );

    /**
     * GetMetadataSearchResults acquires the results from a recording search session previously
     * 				initiated by a FindMetadata operation. The response shall not include results already returned
     * 				in previous requests for the same session. If MaxResults is specified, the response shall not
     * 				contain more than MaxResults results.
     * 				GetMetadataSearchResults shall block until:
     * 				This operation is mandatory to support if the MetaDataSearch capability is set to true in the
     * 				SearchCapabilities structure return by the GetCapabilities command in the Device service.
     */
    @WebMethod(operationName = "GetMetadataSearchResults", action = "http://www.onvif.org/ver10/search/wsdl/GetMetadataSearchResults")
    @RequestWrapper(localName = "GetMetadataSearchResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetMetadataSearchResults")
    @ResponseWrapper(localName = "GetMetadataSearchResultsResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetMetadataSearchResultsResponse")
    @WebResult(name = "ResultList", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public org.onvif.ver10.schema.FindMetadataResultList getMetadataSearchResults(

        @WebParam(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.String searchToken,
        @WebParam(name = "MinResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer minResults,
        @WebParam(name = "MaxResults", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer maxResults,
        @WebParam(name = "WaitTime", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.Duration waitTime
    );

    /**
     * Returns information about a single Recording specified by a RecordingToken. This operation
     * 				is mandatory to support for a device implementing the recording search service.
     */
    @WebMethod(operationName = "GetRecordingInformation", action = "http://www.onvif.org/ver10/search/wsdl/GetRecordingInformation")
    @RequestWrapper(localName = "GetRecordingInformation", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetRecordingInformation")
    @ResponseWrapper(localName = "GetRecordingInformationResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.GetRecordingInformationResponse")
    @WebResult(name = "RecordingInformation", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public org.onvif.ver10.schema.RecordingInformation getRecordingInformation(

        @WebParam(name = "RecordingToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.String recordingToken
    );

    /**
     * FindRecordings starts a search session, looking for recordings that matches the scope (See
     * 				20.2.4) defined in the request. Results from the search are acquired using the
     * 				GetRecordingSearchResults request, specifying the search token returned from this request.
     * 				The device shall continue searching until one of the following occurs:
     * 				The order of the results is undefined, to allow the device to return results in any order they
     * 				are found. This operation is mandatory to support for a device implementing the recording
     * 				search service.
     */
    @WebMethod(operationName = "FindRecordings", action = "http://www.onvif.org/ver10/search/wsdl/FindRecordings")
    @RequestWrapper(localName = "FindRecordings", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.FindRecordings")
    @ResponseWrapper(localName = "FindRecordingsResponse", targetNamespace = "http://www.onvif.org/ver10/search/wsdl", className = "org.onvif.ver10.search.wsdl.FindRecordingsResponse")
    @WebResult(name = "SearchToken", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
    public java.lang.String findRecordings(

        @WebParam(name = "Scope", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        org.onvif.ver10.schema.SearchScope scope,
        @WebParam(name = "MaxMatches", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        java.lang.Integer maxMatches,
        @WebParam(name = "KeepAliveTime", targetNamespace = "http://www.onvif.org/ver10/search/wsdl")
        javax.xml.datatype.Duration keepAliveTime
    );
}
